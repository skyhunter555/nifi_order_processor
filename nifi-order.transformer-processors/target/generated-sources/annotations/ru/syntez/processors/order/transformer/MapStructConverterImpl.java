package ru.syntez.processors.order.transformer;

import javax.annotation.Generated;
import ru.syntez.processors.order.transformer.entities.OrderDocument;
import ru.syntez.processors.order.transformer.entities.OrderDocumentExt;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-02-10T22:54:47+0300",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 1.8.0_241 (Oracle Corporation)"
)
public class MapStructConverterImpl implements MapStructConverter {

    @Override
    public OrderDocumentExt convert(OrderDocument orderDocument) {
        if ( orderDocument == null ) {
            return null;
        }

        OrderDocumentExt orderDocumentExt = new OrderDocumentExt();

        orderDocumentExt.setDocumentId( orderDocument.getDocId() );
        orderDocumentExt.setDocumentType( orderDocument.getDocType() );
        orderDocumentExt.setDocumentDescription( orderDocument.getDocType() );

        return orderDocumentExt;
    }
}
