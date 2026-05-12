package edu.eci.dosw.exception;

import edu.eci.dosw.model.IdentificationType;

public class IdentificationAlreadyRegisteredException extends BusinessException {

    public IdentificationAlreadyRegisteredException(IdentificationType identificationType,
                                                    String identification) {
        super("Identification already registered: "
                + identificationType
                + " "
                + identification);
    }
}