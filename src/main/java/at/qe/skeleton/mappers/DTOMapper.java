package at.qe.skeleton.mappers;

/**
 * DTOMapper Interface. Provides common base methods for all mappers.
 *
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by Innsbruck University.
 * 
 * @param <E> the entity a DTO is mapped from
 * @param <D> the DTO an entity is mapped to
 */
public abstract class DTOMapper<E, D> {
    abstract D mapTo(E entity);
    abstract E mapFrom(D dto);
}
