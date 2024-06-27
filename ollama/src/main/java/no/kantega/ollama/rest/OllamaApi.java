package no.kantega.ollama.rest;

import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/api")
@RegisterRestClient(configKey = "ollama-api")
public interface OllamaApi {
    
    record Models(List<Model> models) {
    }

    record Model(String name, ZonedDateTime modified_at, long size, String digest, Details details) {            
        public String baseName() {
            return name.substring(0, name.indexOf(':'));
        }
        public String tag() {
            return name.substring(name.indexOf(':') + 1);
        }
    }

    record Details(String format, String family, List<String> families, String parameter_size, String quantization_level) {
    }

    record Info(String modelfile, String parameters, String template, Details details) {
    }

    @GET
    @Path("/tags")
    Models getModels();
    
    record ShowParams(String name) {
    }
    
    @POST
    @Path("/show")
    Info getModelInfo(ShowParams params);
}
