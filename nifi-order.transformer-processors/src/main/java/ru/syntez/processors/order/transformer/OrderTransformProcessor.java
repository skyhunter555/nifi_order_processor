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
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.io.StreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import ru.syntez.processors.order.transformer.entities.OrderDocument;
import ru.syntez.processors.order.transformer.entities.OrderDocumentExt;
import ru.syntez.processors.order.transformer.entities.OrderDocumentRoot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * OrderTransformProcessor
 * USE_MAP_STRUCT: boolean:  Do we need to use the map struct library for transformation
 * REL_SUCCESS
 * REL_FAILURE
 * REL_ORIGINAL
 * <p>
 * onTrigger: implemented transformation process
 *
 * @author Skyhunter
 * @date 10.02.2021
 */
@Tags({"example", "transform", "demo"})
@CapabilityDescription("Example demo processor to transform document order entity to other")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute = "", description = "")})
@WritesAttributes({@WritesAttribute(attribute = "", description = "")})
public class OrderTransformProcessor extends AbstractProcessor {

    private static final String USE_MAP_STRUCT_NAME = "USE_MAP_STRUCT";

    public static final PropertyDescriptor USE_MAP_STRUCT = new PropertyDescriptor
            .Builder().name(USE_MAP_STRUCT_NAME)
            .displayName("useMapStruct")
            .description("Do we need to use the map struct library for transformation")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("REL_SUCCESS")
            .description("Success relationship")
            .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("REL_FAILURE")
            .description("Failture relationship")
            .build();

    public static final Relationship REL_ORIGINAL = new Relationship.Builder()
            .name("REL_ORIGINAL")
            .description("Original relationship")
            .build();


    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    private Integer documentCount = 0; //Общее количество обработанных документов
    private ObjectMapper xmlMapper;

    private void initXMLMapper() {
        JacksonXmlModule xmlModule = new JacksonXmlModule();
        xmlModule.setDefaultUseWrapper(false);
        xmlMapper = new XmlMapper(xmlModule);
        ((XmlMapper) xmlMapper).enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    protected void init(final ProcessorInitializationContext context) {

        final List<PropertyDescriptor> descriptors = new ArrayList<>();
        descriptors.add(USE_MAP_STRUCT);
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAILURE);
        relationships.add(REL_ORIGINAL);
        this.relationships = Collections.unmodifiableSet(relationships);
        initXMLMapper();
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {

    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile inputFlowFile = session.get();
        if (inputFlowFile == null) {
            return;
        }

        final boolean useMapStruct = context.getProperty(USE_MAP_STRUCT_NAME).asBoolean();

        final List<OrderDocumentExt> orderDocumentExtList = new ArrayList<>();
        FlowFile originalFlowFile = session.create(inputFlowFile);

        try (InputStream inputStream = session.read(inputFlowFile)) {
            OrderDocumentRoot orderDocumentRoot = xmlMapper.readValue(inputStream, OrderDocumentRoot.class);
            orderDocumentExtList.addAll(transformDocument(orderDocumentRoot.getRoutingDocument(), useMapStruct));
        } catch (Exception ex) {
            getLogger().error("Failed to read XML string: " + ex.getMessage());
            session.write(inputFlowFile);
            session.transfer(inputFlowFile, REL_FAILURE);
        }

        // getLogger().warn("orderDocumentExtList.size(): " + orderDocumentExtList.size());

        List<FlowFile> newFlowFileList = new ArrayList<>();
        for (OrderDocumentExt orderDocumentExt : orderDocumentExtList) {
            FlowFile splitFlowFile = session.create(inputFlowFile);
            try {
                session.write(splitFlowFile, out -> out.write(xmlMapper.writeValueAsBytes(orderDocumentExt)));
                newFlowFileList.add(splitFlowFile);
            } catch (Throwable ex) {
                session.remove(splitFlowFile);
                getLogger().error("Failed to read XML string: " + ex.getLocalizedMessage());
            }
        }

        //getLogger().warn("newFlowFileList.size(): " + newFlowFileList.size());

        session.remove(inputFlowFile);
        for (FlowFile flowFile : newFlowFileList) {
            session.transfer(flowFile, REL_SUCCESS);
        }
        session.transfer(originalFlowFile, REL_ORIGINAL);

        //try {
        //    session.write(inputFlowFile, new OrderTransformCallback(useMapStruct));
        //    session.putAttribute(inputFlowFile, "transformed", "true");
        //    session.putAttribute(inputFlowFile, "useMapStruct", String.valueOf(useMapStruct));
        //    session.transfer(inputFlowFile, REL_SUCCESS);
        //    session.transfer(originalFlowFile, REL_ORIGINAL);
        //} catch (Throwable ex) {
        //    System.out.println("Error " + ex.getMessage());
        //    session.transfer(inputFlowFile, REL_FAILURE);
        //}

    }

    private List<OrderDocumentExt> transformDocument(List<OrderDocument> orderDocumentArray, boolean useMapStruct) {
        List<OrderDocumentExt> orderDocumentExtList = new ArrayList<>();
        for (OrderDocument orderDocument : orderDocumentArray) {
            OrderDocumentExt orderDocumentExt;
            if (useMapStruct) {
                orderDocumentExt = MapStructConverter.MAPPER.convert(orderDocument);
            } else {
                orderDocumentExt = new OrderDocumentExt();
                orderDocumentExt.setDocumentId(orderDocument.getDocId());
                orderDocumentExt.setDocumentType(orderDocument.getDocType());
            }
            documentCount++;
            orderDocumentExt.setDocumentNumber(documentCount);
            orderDocumentExtList.add(orderDocumentExt);
        }
        return orderDocumentExtList;
    }

    //private class OrderTransformCallback implements StreamCallback {

    //    private final ObjectMapper xmlMapper;
    //    private final boolean useMapStruct;

    //    OrderTransformCallback(boolean useMapStruct) {
    //        JacksonXmlModule xmlModule = new JacksonXmlModule();
    //        xmlModule.setDefaultUseWrapper(false);
    //        xmlMapper = new XmlMapper(xmlModule);
    //        ((XmlMapper) xmlMapper).enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
    //        this.useMapStruct = useMapStruct;
    //    }

    //    @Override
    //    public void process(InputStream inputStream, OutputStream outputStream) throws IOException {
    //        OrderDocument[] orderDocumentArray = xmlMapper.readValue(inputStream, OrderDocument[].class);
    //        for (OrderDocument orderDocument : orderDocumentArray) {
    //            OrderDocumentExt orderDocumentExt;
    //            if (useMapStruct) {
    //                orderDocumentExt = MapStructConverter.MAPPER.convert(orderDocument);
    //            } else {
    //                orderDocumentExt = new OrderDocumentExt();
    //                orderDocumentExt.setDocumentId(orderDocument.getDocId());
    //                orderDocumentExt.setDocumentType(orderDocument.getDocType());
    //            }
    //            documentCount++;
    //            orderDocumentExt.setDocumentNumber(documentCount);
    //            outputStream.write(xmlMapper.writeValueAsBytes(orderDocumentExt));
    //        }
    //    }
    //}

}

