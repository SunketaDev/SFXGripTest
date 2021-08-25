package SFX;

public interface ISFXSensorEventHandler {

    void onGyroValueChanged(float norm);

    void onMagnetValueChanged(float norm);
}
