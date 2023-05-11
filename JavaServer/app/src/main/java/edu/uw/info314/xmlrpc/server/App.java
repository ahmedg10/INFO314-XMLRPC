package edu.uw.info314.xmlrpc.server;

import java.util.*;
import java.util.logging.*;
import static spark.Spark.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class Call {
    public String name;
    public List<Object> args = new ArrayList<Object>();
    public boolean fault;
    public String faultCode;
    public String faultString;
}






public class App {
    public static final Logger LOG = Logger.getLogger(App.class.getCanonicalName());
    private static final Calc calc = new Calc();

    public static void main(String[] args) {
        LOG.info("Starting up on port 8080");

        // Set the port to listen on
        port(8080);

        // Return a 404 for any URL other than "/RPC"
        notFound((req, res) -> {
            res.status(404);
            return "Not Found";
        });

        // Return a 405 (Method Not Allowed) for any operation other than POST
        before((request, response) -> {
            if (!request.requestMethod().equalsIgnoreCase("POST")) {
                halt(405, "Method Not Allowed");
            }
        });

        // Handle the POST request with XML-RPC parsing and processing
        post("/RPC", (request, response) -> {
            // Parse the XML-RPC request
            Call call = parseXMLRPC(request.body());

            // Process the XML-RPC request
            String xmlResponse = processXMLRPC(call);

            // Set the response type and status
            response.type("text/xml");
            response.status(200);

            // Return the XML-RPC response
            return xmlResponse;
        });
    }

    private static Call parseXMLRPC(String xml) {
        Call call = new Call();

        // Define the regular expression pattern for the method name tag
        Pattern methodNamePattern = Pattern.compile("<methodName>(.+?)</methodName>");

        // Use a Matcher to find the method name in the XML string
        Matcher methodNameMatcher = methodNamePattern.matcher(xml);
        if (methodNameMatcher.find()) {
            // Extract the method name from the first match group
            call.name = methodNameMatcher.group(1);
        }

        // Define the regular expression pattern for the parameter value tag
        Pattern paramValuePattern = Pattern.compile("<value>(.+?)</value>");

        // Use a Matcher to find each parameter value in the XML string
        Matcher paramValueMatcher = paramValuePattern.matcher(xml);
        while (paramValueMatcher.find()) {
            // Extract the parameter value from the first match group
            String paramValue = paramValueMatcher.group(1);

                // Check if the parameter value starts with "<i4>" and ends with "</i4>"
                if (paramValue.matches("<i4>\\d+</i4>")) {
                    // Extract the integer value and add it to the args list in the Call object
                    int intValue = Integer.parseInt(paramValue.replaceAll("<.*?>", ""));
                    call.args.add(intValue);
                } else { 
                        // Set the fault flag, fault code, and fault string for invalid argument types
                        call.fault = true;
                        call.faultCode = "3";
                        call.faultString = "illegal argument type";
                }

        }

        

        // Return the Call object with the parsed XML-RPC request
        return call;
    }

    private static String generateFaultXML(String faultCode, String faultString){
        return String.format("<methodResponse><fault><value><struct>"
                    + "<member><name>faultCode</name><value><i4>%s</i4></value></member>"
                    + "<member><name>faultString</name><value><string>%s</string></value></member>"
                    + "</struct></value></fault></methodResponse>", faultCode, faultString);
    }
    // Process the XML-RPC request and return the XML-RPC response as a string
    private static String processXMLRPC(Call call) {
        String responseXml = "";
        int result = 0;
        boolean fault = false;
        String faultCode = "";
        String faultString = "";


        if (call.fault) {
            responseXml = generateFaultXML(call.faultCode, call.faultString);
        } else {
        try {
            switch (call.name) {
                case "add":
                if (call.args.size() == 0) {
                    result = 0;
                } else if (call.args.size() == 1) {
                    result = (Integer) call.args.get(0);
                } else {
                    int[] argsArray = new int[call.args.size()];
                    for (int i = 0; i < call.args.size(); i++) {
                        
                        if (call.args.get(i) != null){
                            //used ChatGBT for this -> This code turns to object into a string then Integer.valueOf() to parse the string and return an Integer object. 
                            argsArray[i] = Integer.valueOf(call.args.get(i).toString());
                        }
                       
                    }
                    result = calc.add(argsArray);
                }
                break;
                case "subtract":
                    if (call.args.size() != 2 || !(call.args.get(0) instanceof Integer) || !(call.args.get(1) instanceof Integer)) {
                        fault = true;
                        faultCode = "2";
                        faultString = "Invalid parameters";
                    } else {
                        try {
                            result = calc.subtract((Integer) call.args.get(0), (Integer) call.args.get(1));
                        } catch (ArithmeticException e) {
                            fault = true;
                            faultCode = "3";
                            faultString = "Arithmetic error: " + e.getMessage();
                        }
                    }
                    break;
                case "multiply":
                    if(call.args.size() == 0){
                        result = 1; 
                    } else if (call.args.size() == 1){
                        result = (int) call.args.get(0);
                    } else {
                        int[] argsArray = new int[call.args.size()];
                        for (int i = 0; i < call.args.size(); i++) {
                        
                            if (call.args.get(i) != null){
                                //used ChatGBT for this -> This code turns to object into a string then Integer.valueOf() to parse the string and return an Integer object. 
                                argsArray[i] = Integer.valueOf(call.args.get(i).toString());
                            }

                        }

                        result = calc.multiply(argsArray);
                    }
                    break;
                case "divide":
                    int divisor = (Integer) call.args.get(1);
                    if (divisor == 0) {
                        fault = true;
                        faultCode = "1";
                        faultString = "divide by zero";
                    } else {
                        result = calc.divide((Integer) call.args.get(0), divisor);
                    }
                    break;
                case "modulo":
                    int modDivisor = (Integer) call.args.get(1);
                    if (modDivisor == 0) {
                        fault = true;
                        faultCode = "1";
                        faultString = "divide by zero";
                    } else {
                        result = calc.modulo((Integer) call.args.get(0), modDivisor);
                    }
                    break;
                default:
                    throw new Exception("Unknown method name");
            }
        } catch (Exception e) {
            fault = true;
            faultCode = "3";
            faultString = "illegal argument type";
        }

        if (fault) {
            responseXml = String.format("<methodResponse><fault><value><struct>"
                    + "<member><name>faultCode</name><value><i4>%s</i4></value></member>"
                    + "<member><name>faultString</name><value><string>%s</string></value></member>"
                    + "</struct></value></fault></methodResponse>", faultCode, faultString);
        } else {
            responseXml = String.format("<methodResponse><params><param><value><i4>%d</i4></value></param></params></methodResponse>", result);
        }
    }
    return responseXml;
    }
}
