package es.academia.campushub.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Contador independiente para cada colección que necesita identificadores numéricos. */
@Document(collection = "secuencias")
public record SecuenciaMongo(@Id String id, long valor) {}
