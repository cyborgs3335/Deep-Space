/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands.vision;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import frc.robot.Robot;
import frc.robot.RobotPreferences;
import frc.robot.subsystems.vision.Limelight;

public class VisionDriveRobot extends Command {

  private Limelight mLimelight = Limelight.getInstance();
  //private ButterflyDriveHelper mButterflyDriveHelper = new ButterflyDriveHelper();

  private double lastGoodS = 0;
  private double lastGoodSTime = 0;
  double distanceInches;
  double mFR,mBL,mBR,mFL;
  public VisionDriveRobot() {
    // Use requires() here to declare subsystem dependencies
    // eg. requires(chassis);
    requires(Robot.driveTrain);
  }

  // Called just before this Command runs the first time
  @Override
  protected void initialize() {
    //Grab values

  }

  // Called repeatedly when this Command is scheduled to run
  @Override
  protected void execute() {
    double scew = mLimelight.getSidewaysAngle();
    if(scew!=0&&scew!=-90) {
        lastGoodS = (lastGoodS<-45 ? 90-scew : -scew);
        lastGoodSTime = Timer.getFPGATimestamp();  
    }
    if(Timer.getFPGATimestamp()-lastGoodSTime>5) {
        lastGoodS = 0;
    }

    double angle = mLimelight.getSidewaysAngle();
    distanceInches = mLimelight.getDistance(); //theoreticaly do not need to divide by 10

    //Run distance calculations
    double rotationInches = distanceInches*Math.PI*angle/180;
    double sidewaysInches = distanceInches * Math.tan(lastGoodS) * (lastGoodS<-45 ? 1 : -1);

    //rot = angle/360   x   wheelWidth x pi / circumference
    //Calculate wheel rotations
    double turningRotations = (angle * RobotPreferences.kMecanumWheelWidth) / (360 * RobotPreferences.kDrivetrainWheelDiameterInches);
    double sideRotations = sidewaysInches / (RobotPreferences.kDrivetrainWheelDiameterInches * Math.PI);
    double forwardRotations = distanceInches / (RobotPreferences.kDrivetrainWheelDiameterInches * Math.PI);

    
       mFR = (forwardRotations + sideRotations + turningRotations);
       mFL = (forwardRotations - sideRotations - turningRotations);
       mBR = (forwardRotations - sideRotations + turningRotations);
       mBL = (forwardRotations + sideRotations - turningRotations);

    if(Robot.limelight.hasTarget()){
       Robot.driveTrain.driveMotors(mFR, mBR, mFL, mBL, false);
    }
    
  }

  // Make this return true when this Command no longer needs to run execute()
  @Override
  protected boolean isFinished() {
    if(distanceInches < .8 || distanceInches > -.8){
      return true;
    }
    else{
       return false;
    }
   
    
  }

  // Called once after isFinished returns true
  @Override
  protected void end() {
  }

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  @Override
  protected void interrupted() {
  }
}
