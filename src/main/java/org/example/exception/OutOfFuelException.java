package org.example.exception;

public class OutOfFuelException extends Exception{
    // Hatayı fırlatırken içine bir mesaj koymamızı sağlayan yapıcı metot
    public OutOfFuelException(String message) {
        super(message);
    }
}
