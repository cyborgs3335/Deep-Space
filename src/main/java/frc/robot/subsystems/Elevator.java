/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import com.ctre.phoenix.ErrorCode;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.FollowerType;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.SensorCollection;
import com.ctre.phoenix.motorcontrol.SensorTerm;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.RobotMap;
import frc.robot.RobotPreferences;
import frc.robot.commands.DriveElevator;
//import frc.robot.commands.DriveElevator;
import frc.util.LatchedBoolean;

/**
 * this Subsystem controls the elevator
 * it has two motors with one that follows the other
 * 
 * 
 * 
 */
public class Elevator extends Subsystem implements LoggableSubsystem {

  public enum ElevatorPosition{
		LOWEST_HATCH,
    MEDIUM_HATCH,
    TALL_HATCH;
		
		private static ElevatorPosition[] vals = values();
		public ElevatorPosition next() {
			return vals[(this.ordinal()+1) % vals.length];
    }
  }

  

  public double currentHeight;


  //this magical subsystems creates the motors that control the elevator 
  

  public TalonSRX elevatorMotor1;
  public VictorSPX elevatorMotor2;

  public SensorCollection sensors;
  private ElevatorPosition elevatorHeight;
  private double height;
  private String datHeight = "ground";
  private boolean isSafe; //false is not safe
  private boolean seeEncoder;
 

  private LatchedBoolean tachCrossed = new LatchedBoolean();




  public Elevator(){
    elevatorMotor1 = new TalonSRX(RobotMap.ELEVATOR_MOTOR_1);
    elevatorMotor1.configFactoryDefault(100);
    elevatorMotor2 = new VictorSPX(RobotMap.ELEVATOR_MOTOR_2);
    elevatorMotor2.configFactoryDefault(100);
    
    sensors = elevatorMotor1.getSensorCollection();
    
    seeEncoder = true;
    ErrorCode encoderPresent = elevatorMotor1.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Absolute, 0, 10);
    if(encoderPresent != ErrorCode.OK) {
      DriverStation.reportError("Jesus im dying where is my encoder", false);
      seeEncoder = false;
    }
    elevatorMotor2.follow(elevatorMotor1);
    ErrorCode limitPresent = elevatorMotor1.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyClosed);
    currentHeight = 19;
    
    elevatorMotor1.setNeutralMode(NeutralMode.Brake);
    elevatorMotor2.setNeutralMode(NeutralMode.Brake);
    //TODO add PID settings into elevatorMotor1
    elevatorMotor1.configReverseLimitSwitchSource(LimitSwitchSource.Deactivated, LimitSwitchNormal.Disabled);
    elevatorMotor1.configForwardLimitSwitchSource(LimitSwitchSource.Deactivated, LimitSwitchNormal.Disabled);
    elevatorMotor1.configForwardSoftLimitEnable(false);
    elevatorMotor1.configReverseSoftLimitEnable(false);
    //elevatorMotor1.configReverseSoftLimitThreshold(0);
    isSafe = true; 
    zeroEncoder();
    //elevatorMotor1.overrideLimitSwitchesEnable(false);
    //elevatorMotor1.overrideSoftLimitsEnable(true);
    elevatorHeight = ElevatorPosition.LOWEST_HATCH;
    
    //pid
    elevatorMotor1.config_kP(0, RobotPreferences.ele_kP);
    elevatorMotor1.config_kI(0, RobotPreferences.ele_kI);
    elevatorMotor1.config_kD(0, RobotPreferences.ele_kD);
    
  }


  public void zeroElevatorEncoder(){
   
       while(getSensor() == false){
      moveElevator(.5);
    }
    while(getSensor() == true){
      moveElevator(.5);
    }
    zeroEncoder();
    isSafe = true;
   
  }

  public boolean isSafe(){
    return isSafe;
  }


  public ElevatorPosition getElevatorHeight(){
    return elevatorHeight;
  }

  public void setElevatorPosition(ElevatorPosition EP){
    elevatorHeight = EP;
  }

  public boolean getSensor(){
    return sensors.isFwdLimitSwitchClosed();
  }

  //sets the elevator to the desired hight
  public void setElevatorHeight(double height) {
    if(isSafe()){
       int targetSensorPosition = (int) Math.round(RobotPreferences.kElevatorScalar*(height - 19) / RobotPreferences.kDistancePerRevolution * 4096 * RobotPreferences.kRatioToOutput /.2);
    elevatorMotor1.set(ControlMode.Position, targetSensorPosition);
    currentHeight = height;
    }
   
  }

  public void magicHeightSet(double height){
    if(isSafe()){
      int targetSensorPosition = (int) Math.round(RobotPreferences.kElevatorScalar*(height - 19) / RobotPreferences.kDistancePerRevolution * 4096 * RobotPreferences.kRatioToOutput/ .2);
    elevatorMotor1.set(ControlMode.MotionMagic, targetSensorPosition);
    currentHeight = height;
    }
    
  }


  
  //public static final double LowestHatch = 19;//bottom
 // public static final double LowestCargo = 27.5;
  //public static final double cargoCargo = 40;
 // public static final double MiddleHatch = 47;
 // public static final double MiddleCargo = 55.5;

  public double getTranslateHeight(){
    return ( getEncoder() / RobotPreferences.kElevatorTicksPerInch * .2) + 19;
  }

  public boolean goodElevatorHight(){
    height = (getEncoder() / RobotPreferences.kElevatorTicksPerInch) +19;
    if(height > 18.5 && height < 19.5){
      datHeight = "lowest hatch";
      return true;
    }
    
    if(height > 46.5 && height < 47.5){
      datHeight = "middle Hatch";
      return true;
    }
    
    return false;
  }

  public void zeroEncoder() {
    //this zeros the encoders
    elevatorMotor1.setSelectedSensorPosition(0, 0, 10);
  }

  public int getEncoder(){
    //gets encoder value
    
    return elevatorMotor1.getSelectedSensorPosition(1);//elevatorMotor1.getSelectedSensorPosition();
  }
  /**
   * 
   * @return true if greater positive false if negative
   */
  public boolean getEncoderDirection() {
    //gets the direction of the encoders
    boolean direction = true;
    if(elevatorMotor1.getSelectedSensorPosition() > 0){
      direction = true;
    }
    if(elevatorMotor1.getSelectedSensorPosition() < 0){
      direction = false;
    }

    return direction;

  }
  /**
   * 
   * @return
   * false if it is not closed
   * true if it is closed
   */
  public boolean getLaser(){
     return sensors.isFwdLimitSwitchClosed();

  }
/**
 * 
 * @param speed sets the speed of both motors for the elevator
 */
  public void moveElevator(double speed){
    
   elevatorMotor1.set(ControlMode.PercentOutput,speed);
    //elevatorMotor2.set(ControlMode.PercentOutput,speed);
   // System.out.println("I done did it maybe @ "+speed);
  }

  public void endElevator(){
    elevatorMotor1.set(ControlMode.PercentOutput, 0);
    //elevatorMotor2.set(ControlMode.PercentOutput, 0);
    //System.out.println("I ended my life");
  }
  

  @Override
  public void initDefaultCommand() {
    setDefaultCommand(new DriveElevator());
    // Set the default command for a subsystem here.
    // setDefaultCommand(new MySpecialCommand());
  }

  @Override
  public void log() {
    SmartDashboard.putNumber("elevator encoder", getEncoder());

    //SmartDashboard.putString("elevator position", datHeight);
    SmartDashboard.putBoolean("vator limit switch", getLaser());
   // SmartDashboard.putBoolean("good height",goodElevatorHight() );
    //SmartDashboard.putNumber("elevator real height", getTranslateHeight());
    if(getLaser() == false ){
      zeroEncoder();
    }

  }

  public void timedLoop() {
    if(tachCrossed.update(getLaser())) {
      zeroEncoder();
    }
     SmartDashboard.getString("Elevator height", " " + getElevatorHeight());
  }
}

  


