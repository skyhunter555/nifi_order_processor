package ru.syntez.processors.order.transformer.entities;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * OrderDocument model
 *
 * @author Skyhunter
 * @date 10.02.2021
 */
@XmlRootElement(name = "rootDocument")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class OrderDocumentRoot {
    private List<OrderDocument> routingDocument;
}
