package co.com.claro.osb.pcml.dto;

import java.util.List;

public class ConsumePCML {

    private String hostRR;
    private String userRR;
    private String passRR;
    private String libraries;
    private String connectionTimeOut;
    private String maxConnection;
    private String pcmlProgram;
    private String pcmlFileName;
    private List<Data> data;

    public ConsumePCML() {
    }

    public String getHostRR() {
        return hostRR;
    }

    public void setHostRR(String hostRR) {
        this.hostRR = hostRR;
    }

    public String getUserRR() {
        return userRR;
    }

    public void setUserRR(String userRR) {
        this.userRR = userRR;
    }

    public String getPassRR() {
        return passRR;
    }

    public void setPassRR(String passRR) {
        this.passRR = passRR;
    }

    public String getLibraries() {
        return libraries;
    }

    public void setLibraries(String libraries) {
        this.libraries = libraries;
    }

    public String getConnectionTimeOut() {
        return connectionTimeOut;
    }

    public void setConnectionTimeOut(String connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    public String getMaxConnection() {
        return maxConnection;
    }

    public void setMaxConnection(String maxConnection) {
        this.maxConnection = maxConnection;
    }

    public String getPcmlProgram() {
        return pcmlProgram;
    }

    public void setPcmlProgram(String pcmlProgram) {
        this.pcmlProgram = pcmlProgram;
    }

    public String getPcmlFileName() {
        return pcmlFileName;
    }

    public void setPcmlFileName(String pcmlFileName) {
        this.pcmlFileName = pcmlFileName;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ConsumePCML{" +
                "hostRR='" + hostRR + '\'' +
                ", userRR='" + userRR + '\'' +
                ", passRR='" + passRR + '\'' +
                ", libraries='" + libraries + '\'' +
                ", connectionTimeOut='" + connectionTimeOut + '\'' +
                ", maxConnection='" + maxConnection + '\'' +
                ", pcmlProgram='" + pcmlProgram + '\'' +
                ", pcmlFileName='" + pcmlFileName + '\'' +
                ", data=" + data +
                '}';
    }
}
