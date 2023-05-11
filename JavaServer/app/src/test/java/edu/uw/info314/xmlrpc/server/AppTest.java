// /*
//  * This Java source file was generated by the Gradle 'init' task.
//  */
// package edu.uw.info314.xmlrpc.server;

// import org.junit.Test;
// import static org.junit.Assert.*;

// public class AppTest {
//     @Test public void appCanExtractLegitCall() throws Exception {
//         String xml = //"<?xml version=\"1.0\"?>" + 
//             "<methodCall>" +
//                 "<methodName>examples.getStateName</methodName>" +
//                 "<params><param><value><i4>41</i4></value></param></params>" +
//             "</methodCall>";

//         Call call = App.extractXMLRPCCall(xml);
//         assertEquals("examples.getStateName", call.name);
//         assertEquals(1, call.args.size());
//     }

//     @Test public void appCanExtractLegitAddCall() throws Exception {
//         String xml = //"<?xml version=\"1.0\"?>" + 
//             "<methodCall>" +
//                 "<methodName>add</methodName>" +
//                 "<params>" + 
//                     "<param><value><i4>1</i4></value></param>" + 
//                     "<param><value><i4>2</i4></value></param>" + 
//                 "</params>" +
//             "</methodCall>";

//         Call call = App.extractXMLRPCCall(xml);
//         assertEquals("add", call.name);
//         assertEquals(2, call.args.size());
//         assertEquals(1, (int)call.args.get(0));
//         assertEquals(2, (int)call.args.get(1));
//     }
// }
