import java.io.*;
import java.net.*;
import java.net.http.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.w3c.dom.Node;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.math.BigInteger;



/**
 * This approach uses the java.net.http.HttpClient classes, which
 * were introduced in Java11.
 */
public class Client {
    // private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    public static String server; 
    public static int port;


    private static String extractXML(BufferedReader reader){
        
        try{
            System.out.print("Enter Method Call: ");
            String input = reader.readLine();
            String[] tokens = input.split("\\(");
            String methodName = tokens[0];
            String params[] = tokens[1].replaceAll("\\)", "").split(",");
            
            String xml = null; 
            switch (methodName) {
                case "add":

                for (String param : params) {
                    // Remove leading and trailing whitespace from parameter string
                    param = param.trim();
                    BigInteger num = BigInteger.valueOf(Long.parseLong(param));
                    if (num.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                        throw new ArithmeticException("Overflow error: parameter " + param + " is too large");
                    }
                }
                
                    // BigInteger num1 = BigInteger.valueOf(Long.parseLong(params[0]));
                    // BigInteger num2 = BigInteger.valueOf(Long.parseLong(params[1]));
                    // BigInteger result = num1.add(num2);
                    // if (result.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                    //     throw new ArithmeticException("Overflow error: result is too large");
                    // }
                    xml = formatXMLMethod(methodName, params);
                    break;
                case "subtract":
                    xml = formatXMLMethod(methodName, params);
                    break;
                case "divide":
                    xml = formatXMLMethod(methodName, params);
    
                    break;
                case "multiply":
                    for (String param : params) {
                    // Remove leading and trailing whitespace from parameter string
                        param = param.trim();
                        BigInteger num = BigInteger.valueOf(Long.parseLong(param));
                        if (num.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                            throw new ArithmeticException("Overflow error: parameter " + param + " is too large");
                        }
                    }
                
                    
                    // BigInteger num3 = BigInteger.valueOf(Long.parseLong(params[0]));
                    // BigInteger num4 = BigInteger.valueOf(Long.parseLong(params[1]));

                    // BigInteger resultMultiply = num3.multiply(num4);
                    // if (resultMultiply.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                    //     throw new ArithmeticException("Overflow error: result is too large");
                    // }
                    xml = formatXMLMethod(methodName, params);
                    break;
    
                case "modulo":
                    xml = formatXMLMethod(methodName, params);
                    break;
                default:
                    System.out.println("Invalid method name.");
                    break;
            }
    
            return xml;

        }catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            return null;
        }
    }

    private static String formatXMLMethod(String method, String[] params){
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<methodCall>\n");
        sb.append("  <methodName>" + method + "</methodName>\n");
        sb.append("  <params>\n");
        for (String param : params) {
            //%s is a format specifier 
            sb.append(String.format("    <param><value><i4>%s</i4></value></param>\n", param.trim())); // removing white space with trim()
        }
        sb.append("  </params>\n");
        sb.append("</methodCall>\n");
        return sb.toString();
    }

    private static String sendReqest(String server, int port, String xmlBody) {
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
    
            String url = String.format("http://" + server + ":%s", port + "/RPC");
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "text/xml")
                .header("User-Agent", "YourGroupName")
                .POST(HttpRequest.BodyPublishers.ofString(xmlBody))
                .build();
    
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();
            return responseBody;
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // preserve interrupt status
            System.err.println("Request was interrupted: " + e.getMessage());
            return null;
        }
    }
    
    
    


    private static void printXMLResponse(String xml){
        Pattern paramValuePattern = Pattern.compile("<value>(.+?)</value>");
        Matcher paramValueMatcher = paramValuePattern.matcher(xml);

        Pattern faultPattern = Pattern.compile("<fault>(.+?)</fault>");
        Matcher faultMatcher = faultPattern.matcher(xml);

        if (faultMatcher.find()){
            printFault(xml);
        } else {
            if(paramValueMatcher.find()){
                //extract value
                String paramValue = paramValueMatcher.group(1);
    
                if (paramValue.matches("<i4>\\d+</i4>")) {
                    // Extract the integer value and print it to the console
                    int intValue = Integer.parseInt(paramValue.replaceAll("<.*?>", "")); //non gready match just try to replace <> 
                    System.out.println("The Answer Is: " + intValue);
                
                }

            }
        }
    
    }

    private static void printFault(String xml){

        String faultString = "";
        String faultCode = "";

            // Define the regular expression pattern for the fault code tag
        Pattern faultCodePattern = Pattern.compile("<i4>(.+?)</i4>");

        // Define the regular expression pattern for the fault string tag
        Pattern faultStringPattern = Pattern.compile("<string>(.+?)</string>");

        // Use a Matcher to find the fault code in the XML string
        Matcher faultCodeMatcher = faultCodePattern.matcher(xml);
        if (faultCodeMatcher.find()) {
            // Extract the fault code from the first match group
            faultCode = faultCodeMatcher.group(1);
        }


        // Use a Matcher to find the fault string in the XML string
        Matcher faultStringMatcher = faultStringPattern.matcher(xml);
        if (faultStringMatcher.find()) {
                // Extract the fault string from the first match group and print it to the console
                faultString = faultStringMatcher.group(1);
        }

        System.out.println(String.format("Fault code: %s, Fault string: %s", faultCode, faultString));

    }
    
    
    
    public static void main(String... args) throws Exception {
        
        //Error Handling For the Port and Server. 
        if (args.length != 2) {
            System.err.println("Format Must Be: java Client <server> <port>");
            System.exit(1);
        }

        server = args[0];
        port = Integer.parseInt(args[1]);


        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            String extractedXml = extractXML(reader);
            String responseBody = sendReqest(server, port, extractedXml);
            printXMLResponse(responseBody);
        }


    //     System.out.println(add() == 0);
    //     System.out.println(add(1, 2, 3, 4, 5) == 15);
    //     System.out.println(add(2, 4) == 6);
    //     System.out.println(subtract(12, 6) == 6);
    //     System.out.println(multiply(3, 4) == 12);
    //     System.out.println(multiply(1, 2, 3, 4, 5) == 120);
    //     System.out.println(divide(10, 5) == 2);
    //     System.out.println(modulo(10, 5) == 0);
    // }
    // public static int add(int lhs, int rhs) throws Exception {
    //     return -1;
    // }
    // public static int add(Integer... params) throws Exception {
    //     return -1;
    // }
    // public static int subtract(int lhs, int rhs) throws Exception {
    //     return -1;
    // }
    // public static int multiply(int lhs, int rhs) throws Exception {
    //     return -1;
    // }
    // public static int multiply(Integer... params) throws Exception {
    //     return -1;
    // }
    // public static int divide(int lhs, int rhs) throws Exception {
    //     return -1;
    // }
    // public static int modulo(int lhs, int rhs) throws Exception {
    //     return -1;
    // }

    }

}
