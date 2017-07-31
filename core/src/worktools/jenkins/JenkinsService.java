package worktools.jenkins;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import worktools.jenkins.JobMetadata.Result;
import worktools.jenkins.utils.Utils;

public class JenkinsService {
	
	private static final int SYNC_DELAY = 0;
	private static final int SYNC_INTERVAL = 30;
	private static final TimeUnit SYNC_TIME_UNIT = TimeUnit.SECONDS;

	private final JenkinsDataService dataService;
	
	private Map<String, List<JobMetadata>> jobMap = new ConcurrentHashMap<>();
	private volatile String syncJobCategory = null;
	private final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
	private volatile ScheduledFuture<?> scheduledSyncTask;
	
	public JenkinsService(JenkinsDataService dataService) {
		
		Thread saveJobMapToCacheTask = new Thread(() -> flushMap());
		
		Runtime.getRuntime().addShutdownHook(saveJobMapToCacheTask);
		
		readJobMapFromCache();
		
		this.dataService = dataService;
	}
	
	private String getCacheFileName() {
		return AppConstants.CACHE_ROOT + AppConstants.JOB_MAP_CACHE_FILE;
	}

	private void flushMap() {
		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getCacheFileName()));) {
			Utils.log("Flushing jobMap to disk");
			oos.writeObject(jobMap);
			oos.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void readJobMapFromCache() {
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getCacheFileName()));) {
			Utils.log("Reading jobMap from disk");
			@SuppressWarnings("unchecked")
			Map<String, List<JobMetadata>> map = (Map<String, List<JobMetadata>>) ois.readObject();
			jobMap = new ConcurrentHashMap<>(map);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void track(String category) {
		if(!Objects.equals(syncJobCategory, category)) {
			
			if(scheduledSyncTask != null) {
				scheduledSyncTask.cancel(true);
			}
			scheduledSyncTask = threadPool.scheduleAtFixedRate(() -> refreshJobsCache(category), SYNC_DELAY, SYNC_INTERVAL, SYNC_TIME_UNIT);
			syncJobCategory = category;
		} 
	}

	public JenkinsService() {
		this(new JenkinsDataService());
	}

	List<JobKey> refreshJobsCache(String jobCategory) {
		Utils.log(":: Refreshing Jobs Cache ::");
		List<JobKey> jobs = dataService.fetchJobs(jobCategory);
		List<JobMetadata> jobMetadatas = jobs.stream()
			.map(jobKey -> getJobStatus(jobKey, false))
			.collect(Collectors.toList());
			
		jobMap.put(jobCategory, jobMetadatas);		
		return getJobIds(jobCategory);
	}
	
	public List<JobMetadata> getJobsByCategory(String jobCategory) {
		return Collections.unmodifiableList(jobMap.get(jobCategory));		
	}
	
	public List<JobKey> getJobIds(String jobCategory) {
		return jobMap.get(jobCategory).stream()
					.map(JobMetadata::getJobKey)
					.collect(Collectors.toList());		
	}
	
	public List<JobMetadata> getJobsByOwner(String owner) {
	
		List<JobMetadata> jobsByOwner = new ArrayList<>();
		for(List<JobMetadata> jobs : jobMap.values()) {
			List<JobMetadata> matchingJobs = jobs.stream()
				.filter(job -> Objects.equals(owner.toLowerCase(), job.getOwner().toLowerCase()))
				.collect(Collectors.toList());
			
			jobsByOwner.addAll(matchingJobs);
		}
		
		return jobsByOwner;
	}
	
	public JobMetadata getJobStatus(JobKey jobKey)  {
		
		return getJobStatus(jobKey, true);
	}
	
	private JobMetadata getJobStatus(JobKey jobKey, boolean skipCacheForLiveJobs)  {
		
		Optional<JobMetadata> result = checkInCache(jobKey, skipCacheForLiveJobs);
		
		if(result.isPresent()) {
			Utils.log("Cache HIT.  " + result.get());
			return result.get();
		} else {
			Utils.log("Cache MISS.");
		}
		
		Utils.log("Fetching Job details - " + jobKey);
		
		String response = dataService.fetchJobData(jobKey.getJobId(), jobKey.getJobCategory());
		Document document = Utils.extractDocument(response);
		
		String stringParamXPath = "/workflowRun/action/parameter[@_class='hudson.model.StringParameterValue']";
		
		String owner = extractValuePair(document, stringParamXPath, "GERRIT_CHANGE_OWNER_EMAIL");
		String gerritComment = extractValuePair(document, stringParamXPath, "GERRIT_CHANGE_SUBJECT");
		

		String xpathExpression = "/workflowRun/result";
		NodeList resultNodes = Utils.extractNodes(document, xpathExpression);
		
		JobMetadata jobMetadata = null;
		
		if(resultNodes.getLength() > 0) {
			String textContent = resultNodes.item(0).getTextContent();
			Utils.log("Result status: " + textContent);
			jobMetadata = new JobMetadata(jobKey, textContent, owner, gerritComment);
		} else {
			Utils.log("Result status: " + Result.RUNNING);
			jobMetadata = new JobMetadata(jobKey, Result.RUNNING, owner, gerritComment);
		}
		Utils.log("Cache MISS. " + jobMetadata);
		return jobMetadata;
	}


	private String extractValuePair(Document document, String ownerXPath, String nameNodeText) {
		NodeList paramNodes = Utils.extractNodes(document, ownerXPath);
		
		for(int i=0; i< paramNodes.getLength(); i++) {
			Node paramNode = paramNodes.item(i);
			String name = "";
			String value = "";
			NodeList childNodes = paramNode.getChildNodes();
			
			for(int j=0; j < childNodes.getLength(); j++) {
				
				Node childNode = childNodes.item(j);
				if("name".equals(childNode.getNodeName())) {
					name = childNode.getTextContent();
				} else if("value".equals(childNode.getNodeName())) {
					value = childNode.getTextContent();
				} 
			}
			
			if(nameNodeText.equals(name)) {
				return value;
			}
		}
		return null;
	}


	private Optional<JobMetadata> checkInCache(JobKey jobKey, boolean skipCacheForLiveJobs) {
		Optional<JobMetadata> result = Optional.empty();
		// cache should not be checked
		//	SKIP_CACHE_CHECK_FOR_LIVE_JOBS			JOB_STATUS			FILTER (pick from cache)
		//		true								RUNNING				true
		//		true								SUCCESS				true
		//		false								RUNNING				false
		//		false								SUCCESS				true
		
		// filter = ! ( !checkCacheForLiveJobs && jobMetadata.getResult() == Result.RUNNING )
		
		Predicate<JobMetadata> isThisALiveJobAndCacheShouldNotBeChecked = jobMetadata -> !(jobMetadata.getResult() == Result.RUNNING && !skipCacheForLiveJobs);
		
		if(jobMap.containsKey(jobKey.getJobCategory())) {
			List<JobMetadata> jobs = jobMap.get(jobKey.getJobCategory());
			return jobs.stream()
				.filter(jobMetadata -> Objects.equals(jobMetadata.getJobId(), jobKey.getJobId()))
				.filter(isThisALiveJobAndCacheShouldNotBeChecked)
				.findFirst();
		}
		return result;
	}
	
	
}
