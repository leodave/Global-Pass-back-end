package global_pass.exception.customBookingException;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String id) {
        super("Product not found with id: " + id);
    }
}
