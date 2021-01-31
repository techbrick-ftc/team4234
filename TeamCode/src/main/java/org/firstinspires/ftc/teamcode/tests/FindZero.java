package org.firstinspires.ftc.teamcode.tests;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.geometry.Rotation2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.teamcode.libraries.Alexi;
import org.firstinspires.ftc.teamcode.libraries.CameraAuto;
import org.firstinspires.ftc.teamcode.libraries.TeleAuto;

@Autonomous(name="Find Zero", group="test")
public class FindZero extends LinearOpMode implements TeleAuto {
    Alexi robot = new Alexi();
    CameraAuto drive = new CameraAuto();

    public void runOpMode() {
        robot.init(hardwareMap);

        drive.setUp(robot.motors, robot.angles, robot.camera, robot.imu, AxesReference.EXTRINSIC, telemetry);
        drive.setPose(new Pose2d(-56, 17, new Rotation2d()));

        waitForStart();

        if (opModeIsActive()) {
            robot.camera.start();
            drive.goToPosition(0, 0, this);
            robot.camera.stop();
        }
    }
}
