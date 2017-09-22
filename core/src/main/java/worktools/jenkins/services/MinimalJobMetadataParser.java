package worktools.jenkins.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import worktools.jenkins.models.JobKey;
import worktools.jenkins.models.JobMetadata;
import worktools.jenkins.models.JobMetadata.Result;

public class MinimalJobMetadataParser implements JobMetadataParser {

	private static final Pattern RESULT_PATTERN = Pattern.compile("<result>(\\S+)</result>");
	private static final Pattern OWNER_PATTERN_1 = Pattern.compile("<userId>(\\S+)</userId>");
	private static final Pattern OWNER_PATTERN_2 = Pattern.compile("<authorEmail>(\\S+)</authorEmail>");

	@Override
	public JobMetadata parseJobMetadata(JobKey jobKey, String response) {
		Result result = parseResult(response, RESULT_PATTERN);
		
		String owner = parse(response, OWNER_PATTERN_1, OWNER_PATTERN_2);
		return new JobMetadata(jobKey, result, owner, null, null, 0);
	}

	private Result parseResult(String response, Pattern pattern) {
		String match = parse(response, pattern);
		if(match != null){
			return Result.valueOf(match);
		}
		return Result.UNKNOWN;
	}

	private String parse(String response, Pattern... patterns) {
		for(Pattern pattern : patterns) {
			Matcher matcher = pattern.matcher(response);
			if(matcher.find()) {
				return matcher.group(1);
			}
		}
		return null;
	}

}
