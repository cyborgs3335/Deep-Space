/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.command.Subsystem;
import frc.robot.RobotMap;

/**
 * Add your docs here.
 */
public class Carriage extends Subsystem implements LoggableSubsystem {

    private static final Carriage INSTANCE = new Carriage();

    private final WPI_TalonSRX rollerMotor;
    private final Solenoid pusher;
    private final Solenoid battleAxe;

    // Put methods for controlling this subsystem
    // here. Call these from Commands.

    private Carriage() {
        super();
        rollerMotor = new WPI_TalonSRX(RobotMap.ROLLER_MOTOR);
        pusher = new Solenoid(RobotMap.BUTTERFLY_PCM_MODULE1, RobotMap.PUSHER_CHANNEL);
        battleAxe = new Solenoid(RobotMap.BUTTERFLY_PCM_MODULE1, RobotMap.BATTLE_AXE_CHANNEL);
    }

    public static Carriage getInstance() {
        return INSTANCE;
    }

    // ROLLER
    public void moveRoller(Joystick joystick) {
        moveRollerSpeed(joystick.getRawAxis(1));
    }

    public void moveRollerSpeed(double speed) {
        rollerMotor.set(speed);
    }

    public void stopRoller() {
        rollerMotor.set(0);

    }

    public double getPosition() {
        double p;
        p = rollerMotor.getSelectedSensorPosition();
        return p;
    }

    // SOLINOID PUSHER

    public void setSolenoidValue(Boolean value) {
        pusher.set(value);
    }

    public boolean getSolenoidValue() {
        return pusher.get();
    }

    public void forward() {
        setSolenoidValue(true);
    }

    public void reverse() {
        setSolenoidValue(false);
    }

    // SOLINOID BATTLE AXE

    public void setSolenoidValueBattleAxe(Boolean value) {
        pusher.set(value);
    }

    public boolean getSolenoidValueBattleAxe() {
        return pusher.get();
    }

    public void forwardBatteAxe() {
        setSolenoidValue(true);
    }

    public void reverseBattleAxe() {
        setSolenoidValue(false);
    }

    // encoder

    /*
     * public int getEncoderValue(){ int position; position = rollerMotor.get
     * 
     * }
     */
    @Override
    public void initDefaultCommand() {
        // Set the default command for a subsystem here.
        // setDefaultCommand(new MySpecialCommand());
    }

    @Override
    public void log() {

    }
}
