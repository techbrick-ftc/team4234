package org.firstinspires.ftc.teamcode.tests;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.RobotConfig;

@TeleOp(name="Shooter Test", group="Test")
public class ShooterTest extends LinearOpMode {
    DcMotor shooterMotor = null;

    public void runOpMode() {
        shooterMotor = hardwareMap.get(DcMotor.class, "Shooter Motor");
        shooterMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        waitForStart();

        while (opModeIsActive()) {
            shooterMotor.setPower(RobotConfig.motorSpeed);
            sleep(50);
        }
    }
}