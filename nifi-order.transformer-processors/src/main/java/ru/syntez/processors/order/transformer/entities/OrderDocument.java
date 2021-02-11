package ru.syntez.processors.order.transformer.entities;

import lombok.Data;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * OrderDocument model
 *
 * @author Skyhunter
 * @date 10.02.2021
 */
@XmlRootElement(name = "routingDocument")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class OrderDocument {
    private int docId;
    private String docType;
}
