package flight.reservation.plane;

public final class AircraftFactory {

    private AircraftFactory() {
    }

    public static Aircraft createAircraft(String type, String model) {
        switch (type.toLowerCase()) {
            case "plane":
                return new PassengerPlane(model);
            case "helicopter":
                return new Helicopter(model);
            case "drone":
                return new PassengerDrone(model);
            default:
                throw new IllegalArgumentException(String.format("Aircraft type '%s' is not recognized", type));
        }
    }
}
