package worktools.jenkins.services;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import worktools.jenkins.models.JobKey;
import worktools.jenkins.models.JobMetadata;
import worktools.jenkins.models.JobMetadata.Result;
import worktools.jenkins.utils.Utils;

public class G3JobMetadataParser implements JobMetadataParser {

	@Override
	public JobMetadata parseJobMetadata(JobKey jobKey, String response) {
		JobMetadata jobMetadata = null;
		Document document = Utils.extractDocument(response);
		
		Map<String, String> parameters = extractValuePairs(document, "/workflowRun/action/parameter");
		
		String owner = parameters.get("hudson.model.StringParameterValue_" + "GERRIT_CHANGE_OWNER_EMAIL");
		String gerritComment = parameters.get("hudson.model.StringParameterValue_" + "GERRIT_CHANGE_SUBJECT");
		
		String buildStage = parseBuildStage(parameters, jobKey.getProject());
	
		
		String result =  extractXpath(document, "/workflowRun/result", Result.RUNNING.name());
//		String buildTime = extractXpath(document, "/workflowRun/duration", "0");
		long duration = 0;
		
		/*try{
			duration = Long.parseLong(buildTime);
		} catch(RuntimeException e) {
		}*/
		
		jobMetadata = new JobMetadata(jobKey, result, owner, gerritComment, buildStage, duration);
		
		Utils.log(jobMetadata.toString());
		return jobMetadata;
	}
	
	String extractXpath(Document document, String xpathExpression, String defaultValue) {
		NodeList resultNodes = Utils.extractNodes(document, xpathExpression);
		if(resultNodes.getLength() > 0) {
			return resultNodes.item(0).getTextContent();
		} else {
			return defaultValue;
		}
	}

	Map<String, String> extractValuePairs(Document document, String xpath) {
	
		Map<String, String> map = new LinkedHashMap<>();
		NodeList paramNodes = Utils.extractNodes(document, xpath);
		
		for(int i=0; i< paramNodes.getLength(); i++) {
			Node paramNode = paramNodes.item(i);
			NamedNodeMap attributes = paramNode.getAttributes();
			Node namedItem = attributes.getNamedItem("_class");
			String textContent = namedItem.getTextContent();
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
			map.put(textContent + "_" + name, value);
		}
		return map;
	}

	String parseBuildStage(Map<String, String> extractAllValuePairs, String project) {
		Map<String, String[]> map = new HashMap<>();
		map.put("PROJECT_1", new String[]{"G3Build", "G3Inspect", "G3Deploy", "G3Test", "G3Publish"});
		map.put("NGI", new String[]{"NGIBuild", "NGIDeploy", "NGISmoke", "NGIIntegration", "NGIPublish"});
		
		String[] stages = map.get(project);
		if(stages == null) {
			stages = new String[]{};
		}
		
		List<String> passedStages = Stream.of(stages)
			.filter(stage -> "true".equals(extractAllValuePairs.get("hudson.model.BooleanParameterValue_" + stage)))
			.collect(Collectors.toList());
		
		if(passedStages.isEmpty()) {
			return "G3Build";
		} else {
			return passedStages.get(passedStages.size() - 1);
		}
	}
}
