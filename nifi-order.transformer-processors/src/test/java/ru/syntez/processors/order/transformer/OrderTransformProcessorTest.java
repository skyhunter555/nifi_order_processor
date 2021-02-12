/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.syntez.processors.order.transformer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.syntez.processors.order.transformer.entities.OrderDocument;
import ru.syntez.processors.order.transformer.entities.OrderDocumentExt;
import ru.syntez.processors.order.transformer.entities.OrderDocumentRoot;
import java.util.ArrayList;
import java.util.List;


public class OrderTransformProcessorTest {

    private TestRunner testRunner;

    private ObjectMapper xmlMapper;

    private void initXMLMapper() {
        JacksonXmlModule xmlModule = new JacksonXmlModule();
        xmlModule.setDefaultUseWrapper(false);
        xmlMapper = new XmlMapper(xmlModule);
        ((XmlMapper) xmlMapper).enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Before
    public void init() {
        initXMLMapper();
        testRunner = TestRunners.newTestRunner(OrderTransformProcessor.class);
    }

    @Test
    public void testProcessor() {

        List<OrderDocumentExt> orderDocumentExtList;
        try{
            OrderDocumentRoot orderDocumentRoot = xmlMapper.readValue(this.getClass().getResource("/docs.xml"), OrderDocumentRoot.class);
            String xml =  xmlMapper.writeValueAsString(orderDocumentRoot);
            orderDocumentExtList = transformDocument(orderDocumentRoot.getRoutingDocument(), true);
        } catch (Exception ex) {
            orderDocumentExtList = new ArrayList<>();
            ex.printStackTrace();
        }
        Assert.assertEquals(3, orderDocumentExtList.size());
    }

    private List<OrderDocumentExt> transformDocument(List<OrderDocument> orderDocumentArray,  boolean useMapStruct) {
        List<OrderDocumentExt> orderDocumentExtList = new ArrayList<>();
        for (OrderDocument orderDocument: orderDocumentArray) {
            OrderDocumentExt orderDocumentExt;
            if (useMapStruct) {
                orderDocumentExt = MapStructConverter.MAPPER.convert(orderDocument);
            } else {
                orderDocumentExt = new OrderDocumentExt();
                orderDocumentExt.setDocumentId(orderDocument.getDocId());
                orderDocumentExt.setDocumentType(orderDocument.getDocType());
            }
            orderDocumentExt.setDocumentNumber(0);
            orderDocumentExtList.add(orderDocumentExt);
        }
        return orderDocumentExtList;
    }

}
