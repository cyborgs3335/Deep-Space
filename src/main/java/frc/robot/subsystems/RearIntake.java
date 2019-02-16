/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import java.nio.ByteBuffer;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.SensorCollection;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.revrobotics.CANDigitalInput.LimitSwitch;


import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.RobotMap;
import frc.robot.RobotPreferences;

/**
 * Add your docs here.
 */
public class RearIntake extends Subsystem implements LoggableSubsystem {
  //arm
  private final TalonSRX armMotor;
  public boolean switcher;
  private final Solenoid lock;
  private final I2C limit;
  byte[] lazer = new byte[1];
  double lazerDistance;

  
  //roller
  public final WPI_VictorSPX intakeRollerMotor;
  public SensorCollection sensors;//this is for the lazer limit switch

  


  public RearIntake(){
    //laser stopper thing ask kirby for correct api cause im lazy and dont want
    //to find it on my own

    //update: kirby does not know 

    //arm
    armMotor = new TalonSRX(RobotMap.ARM_MOTOR);
    armMotor.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);
   
    //roller
    intakeRollerMotor = new WPI_VictorSPX(RobotMap.INTAKE_ROLLER_MOTOR);
    sensors = armMotor.getSensorCollection();
    armMotor.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);

    limit = new I2C(RobotMap.LAZER_LIMIT, 0x52);
    //limit = new DigitalInput(RobotMap.LIMIT_SWITCH);
    //lock
    lock = new Solenoid(RobotMap.BUTTERFLY_PCM_MODULE1, RobotMap.LOCK_CHANNEL);

  }

  public void moveArm(Joystick joystick) {
    moveArm(joystick.getRawAxis(1));
    
  }

  //set and moves arm to specific degrees for the arm
  public void armPickup(int degree){
    int targetSensorPosition = (int) Math.round(degree / RobotPreferences.kDistancePerRevolution * 4096 * RobotPreferences.kRatioToOutput);
    armMotor.set(ControlMode.Position, targetSensorPosition);
  }

	public void moveArm(double speed) {
    //zeros the encoder if it crosses in or out of the limit switch
    if(isLimitSwitchClosed() && switcher == false) {
      zeroEncoder();
      setSwitcher();
    }
    else if( isLimitSwitchClosed() == false && switcher == true){
      zeroEncoder();
      setSwitcher();
    }
		armMotor.set(ControlMode.PercentOutput,speed);
  }

  //stops arm
	public void stop() {
    armMotor.set(ControlMode.PercentOutput, 0);
    
  }

  
  //roller
  public void moveIntakeRoller(Joystick joystick) {
    // goes while the limit switch is not activated
    while(stopRollerLazer() == false){
      moveIntakeRollerSpeed(joystick.getRawAxis(1));
      stopRollerLazer();
    }
  }

	public void moveIntakeRollerSpeed(double speed) {
		intakeRollerMotor.set(speed);
	}

	public void stopIntake() {
		intakeRollerMotor.set(0);
		
  }

  //LOCK
  public void toggleLock(){
    if(getLock()){
      lock.set(false);
    }
    else{
      lock.set(true);
    }
  }

  public boolean getLock(){
    return lock.get();
  }

  //encoder
  public int getEncoderValue(){
    int position;
    position = armMotor.getSelectedSensorPosition();
    return position;
  }

  public void zeroEncoder(){
    armMotor.setSelectedSensorPosition(0);
  }

  //limit switch which just so happens to be a LAZER! how cool is that!... coffee rocks!
  
  public boolean isLimitSwitchClosed(){
    boolean closed;
    if(sensors.isFwdLimitSwitchClosed()){
      closed = true;
    }
    else{
      closed = false;
    }
   return closed;
  }

  public void setSwitcher(){
    if(isLimitSwitchClosed()){
      switcher = true;
    }
    else{
      switcher = false;
    }
  }

  //this method reads a byte value from the lazer encoder which holds the distance 
  public double getLazer(){
    limit.read(0x52, 1, lazer);
    ByteBuffer buffer = ByteBuffer.wrap(lazer); //turns the byte value into a double
    lazerDistance =  buffer.getDouble();
    SmartDashboard.putNumber("lazer distance", lazerDistance);
    return lazerDistance;
  }

/**
 * 
 * @return 
 * This method looks at the distance the lazer is reading and sets it to true or false if the 
 * ball is in the roller or not
 *  true = the ball is in the roller
 *  false = the ball is not in the roller
 */
  public boolean stopRollerLazer(){
    boolean isStopped = false;
     if(getLazer() <= 5){
       stopIntake();
       isStopped = true;
     }
     return isStopped;
  }
 

  @Override
  public void initDefaultCommand() {
    // Set the default command for a subsystem here.
    // setDefaultCommand(new MySpecialCommand());
  }

  @Override
  public void log() {

  }
}
