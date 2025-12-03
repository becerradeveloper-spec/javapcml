package co.com.claro.osb.pcml;

import co.com.claro.osb.pcml.dto.ConsumePCML;
import co.com.claro.osb.pcml.dto.ConsumePCMLResponse;
import co.com.claro.osb.pcml.dto.Data;
import co.com.claro.osb.pcml.dto.DataResponse;
import co.com.claro.osb.pcml.util.PcmlException;
import co.com.claro.osb.pcml.util.StringUtil;
import co.com.claro.osb.pcml.util.StringUtilException;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400ConnectionPool;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.SocketProperties;
import com.ibm.as400.data.ProgramCallDocument;

import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Manager {

    private static String PATH_SERVER_PCML = "/applications/config/Oper/PCML/";
    private ProgramCallDocument pcmlDoc;
    private List<DataResponse> dataResponse = new ArrayList();

    public AS400 getConnectionWithPool(ConsumePCML request) throws PcmlException {
        SocketProperties sp = new SocketProperties();
        AS400ConnectionPool pool = new AS400ConnectionPool();
        sp.setSoTimeout(Integer.parseInt(request.getConnectionTimeOut()));
        sp.setKeepAlive(false);
        pool.setSocketProperties(sp);
        pool.setMaxConnections(Integer.parseInt(request.getMaxConnection()));
        AS400 as400 = null;
        as400.setSocketProperties(sp);

        try {
            as400.setThreadUsed(false);
            as400 = pool.getConnection(request.getHostRR(), request.getUserRR(), request.getPassRR());
            return as400;
        } catch (PropertyVetoException ex) {
            throw new PcmlException(ex);
        } catch (Exception e) {
            throw new PcmlException(e);
        }
    }

    public AS400 getConnection(ConsumePCML request) throws PcmlException {
        AS400 as400 = null;

        try {
            as400 = new AS400(request.getHostRR(), request.getUserRR(), request.getPassRR());
            SocketProperties sp = as400.getSocketProperties();
            sp.setSoTimeout(Integer.parseInt(request.getConnectionTimeOut()));
            sp.setKeepAlive(false);
            as400.setSocketProperties(sp);
            as400.setThreadUsed(false);
            return as400;
        } catch (Exception ex) {
            throw new PcmlException(ex);
        }
    }

    private static boolean setLibraries(AS400 as400, String libraries) throws PcmlException {
        try {
            int logLevel = 2;
            int logSeverity = 2;
            CommandCall commandCall = new CommandCall(as400);
            commandCall.run("CHGLIBL LIBL(" + libraries + ")");
            commandCall.run("CHGJOB LOG(" + logLevel + " " + logSeverity + " *SECLVL) LOGCLPGM(*YES) INQMSGRPY(*DFT) LOGOUTPUT(*JOBEND)");
            boolean result = true;
            return result;
        } catch (ErrorCompletingRequestException | IOException | InterruptedException | PropertyVetoException | AS400SecurityException ex) {
            throw new PcmlException(ex);
        }
    }

    public ConsumePCMLResponse consumePCML(ConsumePCML pcml, Charset charset) throws Exception {
        String completePathPCML = "";
        String completeNameFilePCML = pcml.getPcmlFileName() + ".pcml";
        ConsumePCMLResponse consumePCMLResponse = new ConsumePCMLResponse();
        AS400 as400 = this.getConnection(pcml);

        try {
            if (pcml.getLibraries() != null && !setLibraries(as400, pcml.getLibraries())) {
                throw new PcmlException("No se pudo asignar librerias");
            }

            completePathPCML = PATH_SERVER_PCML + completeNameFilePCML;
            InputStream inputstream = this.getTargetStream(completePathPCML, charset);
            this.pcmlDoc = new ProgramCallDocument(as400, completeNameFilePCML, inputstream, (ClassLoader) null, (InputStream) null, 1);
            List<Data> listData = pcml.getData();

            for (Data dataRequest : listData) {
                this.setValue(dataRequest, pcml, dataRequest.getParameter());
            }

            this.pcmlDoc.setThreadsafeOverride(pcml.getPcmlProgram(), true);
            this.pcmlDoc.callProgram(pcml.getPcmlProgram());
            AS400Message[] messageList = this.pcmlDoc.getProgramCall().getMessageList();
            if (messageList != null && messageList.length > 0) {
                StringBuilder buffer = new StringBuilder();

                for (AS400Message message : messageList) {
                    buffer.append(message.getText()).append(System.getProperty("line.separator"));
                }

                throw new PcmlException(buffer.toString());
            }

            List<List<DataResponse>> result = new ArrayList();

            for (Data dataRequest : listData) {
                result.add(this.getValue(dataRequest, pcml, dataRequest.getParameter(), new ArrayList()));
            }

            consumePCMLResponse.setCode("0");
            consumePCMLResponse.setDescription("Proceso exitoso");
            consumePCMLResponse.setData(result);
        } finally {
            if (as400 != null) {
                as400.disconnectService(2);
            }

        }

        return consumePCMLResponse;
    }

    private InputStream getTargetStream(String completePathPCML, Charset charset) throws IOException {
        FileInputStream inputstream = new FileInputStream(completePathPCML);
        InputStreamReader isr = new InputStreamReader(inputstream, charset);
        BufferedReader reader = new BufferedReader(isr);
        char[] charBuffer = new char[8192];
        StringBuilder builder = new StringBuilder();

        int numCharsRead;
        while ((numCharsRead = reader.read(charBuffer, 0, charBuffer.length)) != -1) {
            builder.append(charBuffer, 0, numCharsRead);
        }

        InputStream targetStream = new ByteArrayInputStream(builder.toString().getBytes(charset));
        return targetStream;
    }

    /**
     * Helper genérico: asegura que nunca iteramos más allá del tamaño real
     * del array definido en el PCML (getNumberOfElements).
     */
    private int getSafeArraySize(String fullPcmlPath, int requestedSize) {
        int size = requestedSize;
        try {
            int maxElements = this.pcmlDoc.getNumberOfElements(fullPcmlPath);
            if (maxElements > 0 && size > maxElements) {
                size = maxElements;
            }
        } catch (Exception e) {
            // Si falla, usamos el tamaño solicitado sin romper la lógica original.
            Logger.getLogger(Manager.class.getName()).log(Level.FINE,
                    "No se pudo obtener numberOfElements para " + fullPcmlPath + ": " + e.toString());
        }
        return size;
    }

    private void setValue(Object obj, ConsumePCML pcml, String path) throws Exception {
        String programName = pcml.getPcmlProgram() + ".";
        Data field = (Data) obj;
        if (!field.getType().equals("element") && !field.getType().equals("e")) {
            if (field.getType().equals("structure") || field.getType().equals("s")) {
                new ArrayList();
                List<LinkedHashMap> var21 = (List) field.getValue();

                for (int i = 0; i < var21.size(); ++i) {
                    try {
                        LinkedHashMap hashMap = (LinkedHashMap) var21.get(i);
                        String currentParameter = hashMap.get("parameter").toString();
                        String currentType = hashMap.get("type").toString();
                        Object currentValue = hashMap.get("value");
                        Data data;
                        if (hashMap.get("size") != null) {
                            int size = Integer.parseInt(hashMap.get("size").toString());
                            data = new Data(currentParameter, currentValue, currentType, size);
                        } else {
                            data = new Data(currentParameter, currentValue, currentType);
                        }

                        String currentPath = path + "." + currentParameter;
                        this.setValue(data, pcml, currentPath);
                    } catch (Exception ex) {
                        Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, (String) null, ex);
                    }
                }
            } else if (field.getValue() instanceof String) {
                int[] indices = new int[1];
                int i = 0;

                // usamos el path original para consultar el tamaño real del array en el PCML
                String originalPath = path;
                int requestedSize = field.getSize();
                int safeSize = getSafeArraySize(programName + originalPath, requestedSize);

                for (String parameter = path; i < safeSize; ++i) {
                    indices[0] = i;

                    Object value;
                    try {
                        value = field.getValue();
                        if (!value.equals("")) {
                            path = path + "." + value;
                        }
                    } catch (Exception var16) {
                        value = " ";
                    }

                    this.pcmlDoc.setValue(programName + path, indices, value);
                    path = parameter;
                }
            } else {
                new ArrayList();
                List<Data> var20 = (List) field.getValue();
                int[] indices = new int[1];
                int i = 0;

                String originalPath = path;
                int requestedSize = field.getSize();
                int safeSize = getSafeArraySize(programName + originalPath, requestedSize);

                for (String parameter = path; i < safeSize; ++i) {
                    indices[0] = i;

                    Object value;
                    try {
                        value = ((Data) var20.get(i)).getValue();
                    } catch (Exception var15) {
                        value = " ";
                    }

                    path = path + "." + ((Data) var20.get(i)).getParameter();
                    this.pcmlDoc.setValue(programName + path, indices, value);
                    path = parameter;
                }
            }
        } else {
            Object value = field.getValue();
            if (value == null) {
                value = "";
            }

            Logger.getLogger(Manager.class.getName()).log(Level.INFO,
                    "setting -> {" + programName + path + "} value -> " + value);
            this.pcmlDoc.setValue(programName + path, value);
        }

    }

    private List<DataResponse> getValue(Object obj, ConsumePCML pcml, String path, List<DataResponse> resultList) throws Exception {
        String programName = pcml.getPcmlProgram() + ".";
        Data field = (Data) obj;
        if (!field.getType().equals("element") && !field.getType().equals("e")) {
            if (!field.getType().equals("structure") && !field.getType().equals("s")) {
                List<Object> listArray = new ArrayList();
                if (field.getValue() instanceof String) {
                    int[] indices = new int[1];
                    int i = 0;

                    String originalPath = path;
                    int requestedSize = field.getSize();
                    int safeSize = getSafeArraySize(programName + originalPath, requestedSize);

                    for (String parameter = path; i < safeSize; ++i) {
                        indices[0] = i;

                        try {
                            Object value = field.getValue();
                            if (!value.equals("")) {
                                path = path + "." + value;
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, (String) null, ex);
                        }

                        Object valueAux = this.pcmlDoc.getValue(programName + path, indices);
                        if (!valueAux.toString().equals("")) {
                            listArray.add(this.getArrayPCML(valueAux.toString(), field.getDelimiter()));
                        }

                        path = parameter;
                    }

                    resultList.add(new DataResponse(path, listArray));
                } else {
                    new ArrayList();
                    List<Data> arrayValue = (List) field.getValue();
                    int[] indices = new int[1];
                    int i = 0;

                    String originalPath = path;
                    int requestedSize = field.getSize();
                    int safeSize = getSafeArraySize(programName + originalPath, requestedSize);

                    for (String parameter = path; i < safeSize; ++i) {
                        indices[0] = i;

                        Object value;
                        try {
                            value = this.pcmlDoc.getValue(programName + path, indices);
                        } catch (Exception var17) {
                            value = this.pcmlDoc.getValue(programName + path, indices);
                        }

                        path = path + "." + ((Data) arrayValue.get(i)).getParameter();
                        if (!value.toString().equals("")) {
                            listArray.add(this.getArrayPCML(value.toString(), field.getDelimiter()));
                        }

                        path = parameter;
                    }
                }
            } else {
                new ArrayList();
                List<LinkedHashMap> arrayData = (List) field.getValue();
                List<DataResponse> currentResult = new ArrayList();

                for (int i = 0; i < arrayData.size(); ++i) {
                    try {
                        LinkedHashMap hashMap = (LinkedHashMap) arrayData.get(i);
                        String currentParameter = hashMap.get("parameter").toString();
                        String currentType = hashMap.get("type").toString();
                        Object currentValue = hashMap.get("value");
                        Data data;
                        if (hashMap.get("size") != null) {
                            int size = Integer.parseInt(hashMap.get("size").toString());
                            data = new Data(currentParameter, currentValue, currentType, size);
                        } else {
                            data = new Data(currentParameter, currentValue, currentType);
                        }

                        String currentPath = path + "." + currentParameter;
                        currentResult.addAll(this.getValue(data, pcml, currentPath, new ArrayList()));
                    } catch (Exception ex) {
                        Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, (String) null, ex);
                    }
                }

                DataResponse newDataResponse = new DataResponse(path, currentResult);
                resultList.add(newDataResponse);
            }
        } else {
            Object value = this.pcmlDoc.getValue(programName + path);
            DataResponse currentDataResponse = new DataResponse(path, value);
            resultList.add(currentDataResponse);
        }

        return resultList;
    }

    private Object getArrayPCML(String stringObject, String delimiter) {
        Object[] object = null;
        if (delimiter == null) {
            delimiter = "|";
        }

        if (stringObject.contains(delimiter)) {
            try {
                object = StringUtil.explode(stringObject, delimiter);
            } catch (StringUtilException ex) {
                object = new Object[]{ex.toString()};
            }
        } else {
            System.out.println("Delimiter: " + stringObject);
            object = new Object[]{stringObject};
        }

        return object;
    }
}
