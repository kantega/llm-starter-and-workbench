package no.kantega.llm;

public interface ModelConfiguration<T> {    
    public String modelName();
    //public String modelType();
    //public String modelFamily();

    public T buildModel();

    public record Named<T>(String modelName, T model) implements ModelConfiguration<T> {
        @Override
        public T buildModel() {
            return model;
        }
    }
}
