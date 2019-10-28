package ru.avasilevich.pipe_processor.exceptions

class NotFoundEnvElementException(field: String) : RuntimeException("Element $field is not found in environment")
