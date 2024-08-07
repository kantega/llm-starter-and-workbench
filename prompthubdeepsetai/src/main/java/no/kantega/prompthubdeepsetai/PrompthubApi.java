package no.kantega.prompthubdeepsetai;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

// https://github.com/deepset-ai/prompthub

@RegisterRestClient(configKey = "prompthubdeepsetai-api")
public interface PrompthubApi {
    
    record PrompthubTemplate(String name, List<String> tags, Map<String, List<String>> meta, String version, String text, String description) {
    }

    @GET
    @Path("/prompts")
    List<PrompthubTemplate> getPromptTemplates();
    
    @GET
    @Path("/prompts/{templateName}")
    PrompthubTemplate getPromptTemplate(@PathParam("templateName") String templateName);
    
    @POST
    @Path("/cards/{templateName}")
    String getPromptTemplateDescription(@PathParam("templateName") String templateName);
}
