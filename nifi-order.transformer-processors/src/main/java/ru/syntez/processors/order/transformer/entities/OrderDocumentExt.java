package ru.syntez.processors.order.transformer.entities;

import lombok.Data;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * OrderDocumentExt model
 *
 * @author Skyhunter
 * @date 10.02.2021
 */
@XmlRootElement(name = "orderDocumentExt")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class OrderDocumentExt {
    private int documentId;
    private String documentType;
    private String documentDescription;
}
