package ru.syntez.processors.order.transformer;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import ru.syntez.processors.order.transformer.entities.OrderDocument;
import ru.syntez.processors.order.transformer.entities.OutputDocumentExt;

@Mapper
public interface MapStructConverter {

     MapStructConverter MAPPER = Mappers.getMapper(MapStructConverter.class);

     @Mappings({
             @Mapping(source="docId",   target="documentId"),
             @Mapping(source="docType", target="documentType")
     })
     OutputDocumentExt convert(OrderDocument orderDocument);

}
