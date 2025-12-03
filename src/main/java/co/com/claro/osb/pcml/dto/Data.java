package co.com.claro.osb.pcml.dto;

import java.util.Objects;

public class Data {

    private String parameter;
    private Object value;
    private String type;
    private int size;
    private String delimiter;

    public Data() {
    }

    public Data(String parameter, Object value) {
        this.parameter = parameter;
        this.value = value;
    }

    public Data(String parameter, Object value, String type) {
        this.parameter = parameter;
        this.value = value;
        this.type = type;
    }

    public Data(String parameter, Object value, String type, int size) {
        this.parameter = parameter;
        this.value = value;
        this.type = type;
        this.size = size;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public String toString() {
        return "Data{" +
                "parameter='" + parameter + '\'' +
                ", value=" + value +
                ", type='" + type + '\'' +
                ", size=" + size +
                ", delimiter='" + delimiter + '\'' +
                '}';
    }
}
