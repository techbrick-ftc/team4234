package org.firstinspires.ftc.teamcode.libraries;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.arcrobotics.ftclib.geometry.Translation2d;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.spartronics4915.lib.T265Camera;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import java.util.Arrays;
import java.util.OptionalDouble;

import static java.lang.Math.PI;
import static java.lang.Math.abs;

public class CameraMain {
    private DcMotor[] motors;
    private double[] motorSpeeds;
    private double[] angles;
    private T265Camera camera;
    private Translation2d translation2d;
    private BNO055IMU imu;
    private AxesReference axesReference;
    private final FtcDashboard dashboard = FtcDashboard.getInstance();
    private final TelemetryPacket packet = new TelemetryPacket();

    private Orientation gangles() { return imu.getAngularOrientation(axesReference, AxesOrder.ZYX, AngleUnit.RADIANS); }
    private Telemetry telemetry;

    // Create persistent variables
    boolean xComplete = false;
    boolean yComplete = false;
    boolean turnComplete = false;

    public void setUpInternal(DcMotor[] motors, double[] angles, T265Camera camera, BNO055IMU imu, AxesReference axesReference, Telemetry telemetry) {
        if (motors.length != angles.length) {
            throw new RuntimeException("Motor array length and angle array length are not the same! Check your code!");
        }
        this.motors = motors;
        this.motorSpeeds = new double[motors.length];
        this.angles = angles;
        this.camera = camera;
        this.imu = imu;
        this.axesReference = axesReference;
        this.telemetry = telemetry;
    }

    public boolean goToInternal(double moveX, double moveY, double theta, double speed) {
        // Wrap theta to localTheta
        double localTheta = wrap(theta);
        T265Camera.CameraUpdate up = camera.getLastReceivedCameraUpdate();

        this.translation2d = up.pose.getTranslation();

        double currentX = this.translation2d.getX() / 0.0254;
        double currentY = this.translation2d.getY() / 0.0254;
        double currentTheta = gangles().firstAngle;

        double deltaX = moveX - currentX;
        double deltaY = moveY - currentY;
        double deltaTheta = wrap(localTheta - currentTheta);

        xComplete = abs(deltaX) < 0.2;
        yComplete = abs(deltaY) < 0.2;
        turnComplete = abs(deltaTheta) < 0.2;

        if (xComplete && yComplete && turnComplete) {
            stopWheel();
            return true;
        }

        double driveTheta = Math.atan2(yComplete ? 0 : -deltaY, xComplete ? 0 : deltaX);
        driveTheta += gangles().firstAngle;

        double localSpeed = speed;
        if (abs(deltaX) < 5 && abs(deltaY) < 5) {
            localSpeed *= avg(abs(deltaX), abs(deltaY)) / 10;
        }
        localSpeed = clamp(0.2, 1, localSpeed);

        for (int i = 0; i < this.motors.length; i++) {
            double motorSpeed = Math.sin(this.angles[i] - driveTheta) * localSpeed + deltaTheta;
            if (motorSpeed < 0.1 && motorSpeed > -0.1) { motorSpeed = 0; } else
            if (motorSpeed < 0.2 && motorSpeed > 0.1) { motorSpeed = 0.2; } else
            if (motorSpeed > -0.2 && motorSpeed < -0.1) { motorSpeed = -0.2; }
            this.motorSpeeds[i] = motorSpeed;
        }

        OptionalDouble optionalSpeed = Arrays.stream(motorSpeeds).max();
        double fastestSpeed = optionalSpeed.isPresent() ? optionalSpeed.getAsDouble() : 0;
        boolean scale = fastestSpeed > 1;
        double scaleFactor = 1 / fastestSpeed;
        for (int i = 0; i < this.motors.length; i++) {
            this.motors[i].setPower(scale ? this.motorSpeeds[i] * scaleFactor : this.motorSpeeds[i]);
        }

        writeTelemetry(deltaX, deltaY, driveTheta);

        return false;
    }

    public Orientation getRotation() {
        return gangles();
    }

    public Translation2d getPosition() {
        Translation2d current = camera.getLastReceivedCameraUpdate().pose.getTranslation();
        return new Translation2d(current.getX() / 0.0254, current.getY() / 0.0254);
    }

    private void stopWheel() {
        for (DcMotor motor : motors) {
            motor.setPower(0);
        }
    }

    private double clamp(double min, double max, double value) {
        return Math.max(min, Math.min(value, max));
    }

    private double wrap(double theta) {
        double newTheta = theta;
        while(abs(newTheta) > PI) {
            if (newTheta < -PI) {
                newTheta += 2*PI;
            } else {
                newTheta -= 2*PI;
            }
        }
        return newTheta;
    }

    private double avg(double... inputs) {
        double output = 0;
        for (double input : inputs) {
            output += input;
        }
        output /= inputs.length;
        return output;
    }

    private void writeTelemetry(double deltaX, double deltaY, double driveTheta) {
        if (telemetry == null) { return; }
        packet.put("FR Speed", motors[0].getPower());
        packet.put("RR Speed", motors[1].getPower());
        packet.put("RL Speed", motors[2].getPower());
        packet.put("FL Speed", motors[3].getPower());
        packet.put("Delta X", deltaX);
        packet.put("Delta Y", deltaY);
        packet.put("Drive Theta", driveTheta);
        dashboard.sendTelemetryPacket(packet);
    }
}
