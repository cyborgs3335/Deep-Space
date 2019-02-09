/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands.drive;

import edu.wpi.first.wpilibj.DriverStation;
import frc.robot.Robot;

/**
 * Implements mecanum drive style.
 */
public class MecanumDrive extends AbstractDriveStyle {

    // Not a command

    public MecanumDrive() {
        super();
    }

    @Override
    public void execute() {
        double rf, rb, lf, lb;
        double y = Robot.oi.getDriveOperator().getDriveForward();
        double x = Robot.oi.getDriveOperator().getDriveRotation();
        double z = Robot.oi.getDriveOperator().getDriveSideway();
        double forward = map(y);
        double right = map(x);
        double clockwise = map(z);
        // double K = .1;//the value that determines sensitivity of turning tweek to
        // edit
        // clockwise = K*clockwise;
        // inverse kinematics

        rf = forward + clockwise + right;
        lf = forward - clockwise - right;
        lb = forward + clockwise - right;
        rb = forward - clockwise + right;

        boolean skidSteerDrive = false;

        // the driving force that drives the mecanum drive ...im tired
        Robot.driveTrain.driveMotors(rf, rb, lf, lb, skidSteerDrive);
        DriverStation.reportWarning("you are in mecanum drive now", true);
    }
}
