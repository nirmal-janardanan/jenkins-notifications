package worktools.jenkins.services;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import worktools.jenkins.AppConstants;
import worktools.jenkins.models.JobKey;
import worktools.jenkins.models.JobMetadata;
import worktools.jenkins.models.JobMetadata.Result;
import worktools.jenkins.models.JobSearchKey;
import worktools.jenkins.utils.Utils;

public class CachedJenkinsDataService implements JenkinsDataService {

	private final JenkinsDataService dataService;
	private final Cache cache;

	private int staleTimeoutInSeconds = 30;
	public CachedJenkinsDataService(JenkinsDataService dataService) {
		this(dataService, getDefaultCacheDirectory());
	}

	public CachedJenkinsDataService(JenkinsDataService dataService, String cacheDirectory) {

		this.dataService = dataService;
		cache = new Cache(cacheDirectory);
		registerShutdownHook();
		cache.init();
	}

	@Override
	public JobMetadata fetchJobData(JobKey jobKey) {

		JobMetadata jobMetadata = cache.getJobMetadataCache().get(jobKey);
		if (jobMetadata == null || jobMetadata.getResult() == Result.RUNNING)
			return fetchJobDataAndUpdate(jobKey);

		return jobMetadata;
	}

	private JobMetadata fetchJobDataAndUpdate(JobKey jobKey) {
		JobMetadata liveJobMetadata = dataService.fetchJobData(jobKey);
		if (liveJobMetadata != null) {
			cache.getJobMetadataCache().put(jobKey, liveJobMetadata);
		}
		return liveJobMetadata;
	}

	@Override
	public List<JobKey> fetchJobs(String project, String jobCategory) {

		CachedJobKeys cachedJobKeys = cache.getJobKeyCache().get(jobCategory);
		if (cachedJobKeys == null || cachedJobKeys.isStale(this.staleTimeoutInSeconds)) {
			return fetchJobsAndUpdate(project, jobCategory);
		}

		return cachedJobKeys.toJobKeys();
	}

	private List<JobKey> fetchJobsAndUpdate(String project, String jobCategory) {
		List<JobKey> jobKeys = dataService.fetchJobs(project, jobCategory);
		this.cache.jobKeyCache.put(jobCategory, new CachedJobKeys(jobKeys));
		return jobKeys;
	}
	
	@Override
	public JobMetadata fetchJob(JobSearchKey jobSearchKey) {
		String owner = jobSearchKey.getOwner();
		String jobCategory = jobSearchKey.getJobCategory();
		List<JobKey> allJobsForCategory = fetchJobs(jobSearchKey.getProject(), jobCategory);
		List<JobKey> jobsTrigerredByOwner = allJobsForCategory.stream()
				.filter(jobKey -> filterJobsTrigerredByOwner(jobKey, owner))
				.collect(Collectors.toList());
		
		return jobsTrigerredByOwner.stream()
				.map(jobKey -> this.fetchJobData(jobKey))
				.filter(Objects::nonNull)
				.peek(jobMetadata -> cache.getJobOwnerCache().putIfAbsent(jobMetadata.getJobKey(), jobMetadata.getOwner()))
				.filter(jobMetadata -> owner.equalsIgnoreCase(jobMetadata.getOwner()))
				.findFirst()
				.orElse(JobMetadata.empty());
	}

	void setJobMetadataCache(Map<JobKey, JobMetadata> map) {
		this.cache.jobMetadataCache = map;
	}

	void setJobKeyCache(Map<String, CachedJobKeys> jobKeyCache) {
		this.cache.jobKeyCache = jobKeyCache;
	}

	public void setStaleTimeoutInSeconds(int duration) {
		this.staleTimeoutInSeconds = duration;
	}

	private static String getDefualtCacheFileName() {
		return System.getProperty("user.home") + "/new_" + AppConstants.JOB_MAP_CACHE_FILE;
	}
	
	private static String getDefaultCacheDirectory() {
		return System.getProperty("user.home") + "/jenkins_notifications_cache";
	}

	private void registerShutdownHook() {
		Thread saveJobMapToCacheTask = new Thread(() -> cache.save());
		Runtime.getRuntime().addShutdownHook(saveJobMapToCacheTask);
	}

	public void clearCache() {
		cache.clear();
	}

	private boolean filterJobsTrigerredByOwner(JobKey jobKey, String owner) {
		Map<JobKey, String> jobOwnerCache = cache.getJobOwnerCache();
		if (jobOwnerCache.containsKey(jobKey)) {
			return owner.equalsIgnoreCase(jobOwnerCache.get(jobKey));
		}
		return true;
	}

	static class CachedJobKeys {
		private final List<JobKey> jobKeys;
		private final long cachedAtTimeStamp;

		public CachedJobKeys(List<JobKey> jobKeys) {
			this(jobKeys, System.currentTimeMillis());
		}

		public CachedJobKeys(List<JobKey> jobKeys, long timeStamp) {
			this.jobKeys = jobKeys;
			this.cachedAtTimeStamp = timeStamp;
		}

		public boolean isStale(int staleTimeoutInSeconds) {
			return (System.currentTimeMillis() - cachedAtTimeStamp) > staleTimeoutInSeconds * 1000;
		}

		public List<JobKey> toJobKeys() {
			return jobKeys;
		}
	}	
	
	public static class Cache implements Serializable{

		private static final long serialVersionUID = 5564676405710851740L;
		private Map<JobKey, JobMetadata> jobMetadataCache = new ConcurrentHashMap<>();
		private Map<String, CachedJobKeys> jobKeyCache = new ConcurrentHashMap<>();
		private Map<JobKey, String> jobOwnerCache = new ConcurrentHashMap<>();
		private final String jobMetadataCacheFileName;
		private final String jobOwnerCacheFileName;
		
		public Cache(String cacheDirectory) {
			jobMetadataCacheFileName = cacheDirectory + "/jobMetadata.cache";
			jobOwnerCacheFileName = cacheDirectory + "/jobOwner.cache";
			
			if(!Files.exists(Paths.get(cacheDirectory))) {
				try {
					Utils.log("Creating cache directory - %s", cacheDirectory);
					Files.createDirectories(Paths.get(cacheDirectory));
				} catch (IOException e) {
					// swallow
					Utils.log("Creating cache directory - %s failed with error %s ", cacheDirectory, e.getMessage());
				}
			}
		}

		void save() {
			Utils.log("Flushing jobMetadataCache to disk");
			writeToCacheFile(jobMetadataCache, jobMetadataCacheFileName);
			
			Utils.log("Flushing jobOwnerCache to disk");
			writeToCacheFile(jobOwnerCache, jobOwnerCacheFileName);
		}
		
		<U,V> void writeToCacheFile(Map<U, V> cacheMap, String cacheFileName) {
			try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cacheFileName))) {
				oos.writeObject(cacheMap);
				oos.flush();
			} catch (IOException e) {
				System.err.println("Could not write to cache file - " + cacheFileName);
				e.printStackTrace();
			}
		}

		void loadFromCacheFiles() {
			jobMetadataCache = readFromCacheFile(jobMetadataCacheFileName);
			jobOwnerCache = readFromCacheFile(jobOwnerCacheFileName);
		}
		
		<U, V> Map<U, V> readFromCacheFile(String cacheFileName) {
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cacheFileName));) {
				Utils.log("Reading jobMetadataCache from disk");
				@SuppressWarnings("unchecked")
				Map<U, V> map = (Map<U, V>) ois.readObject();
				return new ConcurrentHashMap<>(map);
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				return new ConcurrentHashMap<>();
			}
		}

		public void init() {
			loadFromCacheFiles();
		}

		Map<JobKey, JobMetadata> getJobMetadataCache() {
			return jobMetadataCache;
		}

		public void clear() {
			jobMetadataCache.clear();
			jobOwnerCache.clear();
		}

		public Map<String, CachedJobKeys> getJobKeyCache() {
			return jobKeyCache;
		}
		
		public Map<JobKey, String> getJobOwnerCache() {
			return jobOwnerCache;
		}

	}
}