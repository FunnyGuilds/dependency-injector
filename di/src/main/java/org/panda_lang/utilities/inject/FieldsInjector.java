package org.panda_lang.utilities.inject;

/*
TODO: Make FieldsInjector independent from ConstructorInjector (allow to inject fields to any object)
Currently it's just ConstructorInjector with additional fields injection
 */
public interface FieldsInjector<T> extends ConstructorInjector<T> {

    /**
     * Create a new instance of the specified constructor and inject their fields
     *
     * @param injectorArgs arguments for injector
     * @return new instance of the specified type
     * @throws Exception if anything happens during the instance creation
     */
    @Override
    T newInstance(Object... injectorArgs) throws Exception;

}
