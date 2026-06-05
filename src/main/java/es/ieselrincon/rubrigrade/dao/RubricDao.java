package es.ieselrincon.rubrigrade.dao;

import es.ieselrincon.rubrigrade.model.Rubric;

public class RubricDao extends GenericDao<Rubric> {

    public RubricDao() {
        super(Rubric.class);
    }

    // Por ahora con el CRUD del genérico es suficiente.
    // Si más adelante hace falta una consulta especial, se añade aquí.
}
