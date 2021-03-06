package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
//import com.revrobotics.CANSparkMax;
//import com.revrobotics.CANSparkMax.IdleMode;
//import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Sendable;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.OI;
import frc.robot.Robot;
import frc.robot.RobotMap;
import frc.robot.RobotPreferences;
import frc.robot.commands.MecanumDriveTrain;
import frc.robot.commands.vision.PathIntent;
import frc.robot.commands.WhatDriveTrain;
import frc.robot.subsystems.vision.Limelight;
import frc.robot.subsystems.vision.Limelight.Target;


public class DriveTrain extends Subsystem implements LoggableSubsystem {
	//TODO comment out like this entire class for testing

	public enum Target{
		HATCH,
		CARGO;
		
		private static Target[] vals = values();
		public Target next() {
			return vals[(this.ordinal()+1) % vals.length];
		}
	}
	public enum Height{
		LOW,
		MID,
		CSHIP;
		
		private static Height[] vals = values();
		public Height next() {
			return vals[(this.ordinal()+1) % vals.length];
		}
	}
	
	public enum DriveTrainMode{
		ARCADE,
		//TANK,
		ROBOT_ORIANTED_MECANUM;
		//FEILD_ORIANTED_MECANUM;
		
		private static DriveTrainMode[] vals = values();
		public DriveTrainMode next() {
			return vals[(this.ordinal()+1) % vals.length];
		}
		/*
		public DriveModeState last() {
			return vals[(this.ordinal()-1) % vals.length];
		}*/
	}

	public enum DriveState{
		MECANUM,
		SKIDSTEER;

		private static DriveState[] vals = values();
		public DriveState next() {
			return vals[(this.ordinal()+1) % vals.length];
		}
		/*
		public DriveModeState last() {
			return vals[(this.ordinal()-1) % vals.length];
		}*/
	}
	

	// todo find out about encoders for spark motor contorler
	//creates motors
	public final TalonSRX  motorLeft1;
	public final TalonSRX  motorLeft2;
	public final TalonSRX  motorRight1, motorRight2;
	private final Solenoid solenoid1,solenoid2;
	//public final MecanumDrive drive;
	private int direction = RobotMap.DRIVE_TRAIN_FORWARD_DIRECTION;
	private DriveTrainMode mDriveTrain;
	private final double voltageRampRateDefault = 150;
	boolean skidSteer = false;
	
	
	

	public DriveTrainMode getDriveTrain() {
		return mDriveTrain;
	}

	public void setDriveTrain(DriveTrainMode dms) {
		mDriveTrain = dms;
		
	}


//mecanum and skid steer
	private DriveState mDriveState;

	public  DriveState getDriveState() {
		return mDriveState;
	}

	public boolean getDriveState2(){
		return skidSteer;
	}

	


	
// not toggle
	public void setDriveState(DriveState ds) {
		mDriveState = ds;
		if(mDriveState==DriveState.MECANUM) {
			//Robot.driveTrain.switchState(false);
			solenoid1.set(false);
			solenoid2.set(false);
		}else {
			//Robot.driveTrain.switchState(true);
			if(mDriveTrain == DriveTrainMode.ROBOT_ORIANTED_MECANUM){
				solenoid1.set(false);
				solenoid2.set(false);
			}
			else{
				solenoid1.set(true);
				solenoid2.set(true);
			}
			
		}
	}
//toggle
	public void ToggleDriveState() {
			if(skidSteer) {
			//changes to mecanum
			skidSteer = false;
			solenoid1.set(false);
			solenoid2.set(false);
			//Robot.driveTrain.switchState(false);
			
		}
		else {
		//	Robot.driveTrain.switchState(true);

		/* i done change this litter bit
			if(mDriveTrain == DriveTrainMode.ROBOT_ORIANTED_MECANUM){
				solenoid1.set(false);
				solenoid2.set(false);
			}
			*/
			
				skidSteer = true;
				solenoid1.set(true);
				solenoid2.set(true);
			
			
		}
		SmartDashboard.putBoolean("driveState", skidSteer);
	}



	
	//initilizes the motors
	public DriveTrain() {
        super();
        
		motorLeft1 = new TalonSRX (RobotMap.MOTOR_DRIVE_LEFT1);
		motorLeft2 = new TalonSRX  (RobotMap.MOTOR_DRIVE_LEFT2);
		motorRight1 = new TalonSRX  (RobotMap.MOTOR_DRIVE_RIGHT1);
        motorRight2 = new TalonSRX  (RobotMap.MOTOR_DRIVE_RIGHT2);
        
		solenoid1 = new Solenoid(RobotMap.BUTTERFLY_PCM_MODULE1, RobotMap.BUTTERFLY_FORWARD_CHANNEL1);
		solenoid2 = new Solenoid(RobotMap.BUTTERFLY_PCM_MODULE1, RobotMap.BUTTERFLY_FORWARD_CHANNEL2);
		double voltageRampRate = voltageRampRateDefault;//20;
		setRampRate(voltageRampRate);
		solenoid1.set(false);
        solenoid2.set(false);
        
        
		motorLeft1.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);
		motorLeft2.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);
		motorRight1.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);
		motorRight2.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);
		motorRight1.setInverted(true);
		motorRight2.setInverted(true);
        
		//PID

		motorLeft1.config_kD(0, RobotPreferences.kD);
		motorLeft2.config_kD(0, RobotPreferences.kD);
		motorRight1.config_kD(0, RobotPreferences.kD);
		motorRight2.config_kD(0, RobotPreferences.kD);
		motorLeft1.config_kP(0, RobotPreferences.kP);
		motorLeft2.config_kP(0, RobotPreferences.kP);
		motorRight1.config_kP(0, RobotPreferences.kP);
		motorRight2.config_kP(0, RobotPreferences.kP);
		motorLeft1.config_kI(0, RobotPreferences.kI);
		motorLeft2.config_kI(0, RobotPreferences.kI);
		motorRight1.config_kI(0, RobotPreferences.kI);
		motorRight2.config_kI(0, RobotPreferences.kI);

		double output = .75;

		motorLeft1.configPeakOutputForward(output); //30 is timeout
		motorLeft2.configPeakOutputForward(output);
		motorRight1.configPeakOutputForward(output);
		motorRight2.configPeakOutputForward(output);
		motorLeft1.configPeakOutputReverse(-output);
		motorLeft2.configPeakOutputReverse(-output);
		motorRight1.configPeakOutputReverse(-output);
		motorRight2.configPeakOutputReverse(-output);

		//mecanum
		/*
		SpeedControllerGroup left1 = new SpeedControllerGroup(motorLeft1);//left front
		SpeedControllerGroup left2 = new SpeedControllerGroup(motorLeft2);//left back
		SpeedControllerGroup right1 = new SpeedControllerGroup(motorRight1);//right front
		SpeedControllerGroup right2 = new SpeedControllerGroup(motorRight2);//right back
		
		//TankDrive and arcade drive
		SpeedControllerGroup leftGroup = new SpeedControllerGroup(motorLeft1,motorLeft2);//left front
		SpeedControllerGroup rightGroup = new SpeedControllerGroup(motorRight1,motorRight2);//left back
		*/

		mDriveTrain = DriveTrainMode.ROBOT_ORIANTED_MECANUM;
		mDriveState = DriveState.MECANUM;

		//motorLeft1.setSelectedSensorPosition(0, 0, 0);



	}
	
	public void setRampRateTime(double secondsFromNeutralToFull) {
		// See https://github.com/CrossTheRoadElec/Phoenix-Documentation#installing-phoenix-framework-onto-your-frc-robot
		// TODO also see section on limiting current rate, both peak and continuous
		// TODO which will be useful for climbing motors
		// (2, 0) ramps from neutral to full voltage in 2 sec, with no timeout
        
        

        //uncomment this 
        
		motorLeft1.configOpenloopRamp(secondsFromNeutralToFull, 0);
		motorLeft2.configOpenloopRamp(secondsFromNeutralToFull, 0);
        
        
		motorRight1.configOpenloopRamp(secondsFromNeutralToFull, 0);
        motorRight2.configOpenloopRamp(secondsFromNeutralToFull, 0);
        
	}

	@Deprecated
	public void setRampRate(double voltageRampRate){
		// Formerly: frontLeft.setVoltageRampRate(voltageRampRate);
		//    where voltageRampRate was in volts/sec???
		//          or was it in percent voltage / sec ???
		//    used 150 as default, and 5 for slow rate during auto
		// Assuming volts/sec, then
		//     150 V/sec is nominal 12V / 150 V/sec = 0.08 sec
		//     5 V/sec is nominal 12V / 5 V/sec = 2.4 sec
		setRampRateTime(12.0 / voltageRampRate);
	}

	public void setDefaltRampRate(){
		double voltageRampRate = voltageRampRateDefault;
		setRampRate(voltageRampRate);
	}

	
	//sets the break mode for each motor
	public void setBrake(boolean brake) {
		// Formerly: frontLeft.enableBrakeMode(brake);
		// See https://github.com/CrossTheRoadElec/Phoenix-Documentation#installing-phoenix-framework-onto-your-frc-robot
		NeutralMode mode = brake ? NeutralMode.Brake : NeutralMode.Coast;

        //uncoment this
        
		motorLeft1.setNeutralMode(mode);
		motorLeft2.setNeutralMode(mode);
		
		motorRight1.setNeutralMode(mode);
        motorRight2.setNeutralMode(mode);
        
	}


	// i know its messy but i didnt want to think of a way to compact it
    // i will fix it later but im lazy and dont want to right now
    
    

	public double getMotorLeft1Postition(){
		return motorLeft1.getSelectedSensorPosition();
	}

	public double getMotorLeft2Position(){
		return motorLeft2.getSelectedSensorPosition();
	}

	public double getMotorRight1Position(){
		 
		return motorRight1.getSelectedSensorPosition();
	}

	public double getMotorRight2Position(){
		 
		return motorRight2.getSelectedSensorPosition();
    }
    

	public void setDirection(int direction) {
		this.direction = direction * RobotMap.DRIVE_TRAIN_FORWARD_DIRECTION;
	}
	
	/**
	 * 
	 * @param state True = Skid Steer False = Mecanum
	 */
	public void switchState(boolean state){
		solenoid1.set(state);
		solenoid2.set(state);
		
	}

	public Boolean getState(){
		Boolean s = solenoid1.get();
		return s;
	}
	/*
	public void toggleDrive(){
		if(getState()){
			switchState(false);
		}
		else if(getState() == false){
			switchState(true);
		}
	}
	

*/

	

	@Override
	protected void initDefaultCommand() {
		setDefaultCommand(new WhatDriveTrain(mDriveTrain));
		//testing teperalraly changing default command
		//setDefaultCommand(new MecanumDriveTrain());
		// TODO Auto-generated method stub

	}
	/**
	 * 
	 * @param rf    the right front
	 * @param rb	the right back
	 * @param lf	the left front
	 * @param lb	the left back
	 * @param skidSteerDrive true = in skid steer  false = not in skid steer
	 */
	//what is being seen by the mecanumDriveTrain class 
	
	public void driveMotors(double rf, double rb, double lf, double lb, boolean skidSteerDrive) {
		if(skidSteerDrive) {
			if(!(Math.abs(lf-lb)<=.01)){
				lb=lf;
			}
			if(!(Math.abs(rf-rb)<=.01)){
				rb=rf;
			}
        }
        
        
		motorLeft1.set(ControlMode.PercentOutput, lf);
		motorLeft2.set(ControlMode.PercentOutput,lb);
		motorRight1.set(ControlMode.PercentOutput,rf);
		motorRight2.set(ControlMode.PercentOutput,rb);
        

	}

	/**
	 * 
	 * @param rf    the right front
	 * @param rb	the right back
	 * @param lf	the left front
	 * @param lb	the left back
	 * @param skidSteerDrive true = in skid steer  false = not in skid steer
	 */
	//what is being seen by the mecanumDriveTrain class 
	
	public void driveMotorsVision(double rf, double rb, double lf, double lb, boolean skidSteerDrive) {
		int irf = (int) rf*4096;
		int irb = (int) rb*4096;
		int ilf = (int) lf*4096;
		int ilb = (int) lb*4096;
		
		if(skidSteerDrive) {
			if(!(Math.abs(lf-lb)<=.01)){
				lb=lf;
			}
			if(!(Math.abs(rf-rb)<=.01)){
				rb=rf;
			}
		}
		
        
		motorLeft1.set(ControlMode.Position, motorLeft1.getSelectedSensorPosition() + ilf);
		motorLeft2.set(ControlMode.Position, motorLeft2.getSelectedSensorPosition() + ilb);
		motorRight1.set(ControlMode.Position, motorRight1.getSelectedSensorPosition() + irf);
		motorRight2.set(ControlMode.Position, motorRight2.getSelectedSensorPosition() + irb);
        

	}


	//vision 

	

   

    /*
    public DriveIntent pureVisionDriveRaw(Target target){
        if(mLimelight.getTargetSelected()!=target) {
            mLimelight.setPipeline(target);
        }/*
        if(mLimelight.isTarget()){
            return DriveIntent.MECANUM_BRAKE;
        }*/
        /*
        if(false)return oldWay();
        else return smartPath();
    }

  */

   
   
	@Override
	public void log(){
		SmartDashboard.putString("DriveMode", "" + getDriveTrain());
		SmartDashboard.getString("DriveState", "" + getDriveState());
		
		//SmartDashboard.putNumber("front left motor", motorLeft1.get());
		//SmartDashboard.putNumber("back left motor", motorLeft2.get());
		//SmartDashboard.putNumber("Front Right motor", motorRight1.get());
		//SmartDashboard.putNumber("back Right motor", motorRight2.get());
		SmartDashboard.putNumber("joystick x", Robot.oi.getJoystick().getRawAxis(4));
        SmartDashboard.putNumber("joystick y", Robot.oi.getJoystick().getRawAxis(5));
        
        //uncoment this
        
		SmartDashboard.putNumber("encoder FL", getMotorLeft1Postition());
		SmartDashboard.putNumber("encoder FR", getMotorRight1Position());
		SmartDashboard.putNumber("encoder BL", getMotorLeft2Position());
		SmartDashboard.putNumber("encoder BR", getMotorRight2Position());
        

		
	}

	/*
	//what is driving the robot
	public void driveMecanum(Joystick joystick) {
		
		drive.driveCartesian(Robot.oi.stick.getX(), Robot.oi.stick.getY(), Robot.oi.stick.getZ(), 0);
		
		Timer.delay(0.01);
		
		
	}
	*/

}
