package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Subsystem;
// import edu.wpi.first.wpilibj.Timer;
// import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import static frc.robot.RobotMap.*;
import frc.robot.IO;

/**
 * The Shooter is an abstraction of the real-life system of three belts run by REV NEOs that run our shooting mechanism. It is an example of an FRC "Subsystem".
 */

public class Shooter implements Subsystem {
    private CANSparkMax shootMtr = new CANSparkMax(ID_SHOOTER_1, MotorType.kBrushless);
    private CANSparkMax shootMtr1 = new CANSparkMax(ID_SHOOTER_2, MotorType.kBrushless);
    private CANSparkMax shootMtr2 = new CANSparkMax(ID_SHOOTER_3, MotorType.kBrushless);
    private CANSparkMax shootMotors[] = new CANSparkMax[]{shootMtr, shootMtr1, shootMtr2};
    private SparkMaxPIDController shootPIDs[] = new SparkMaxPIDController[3];
    private RelativeEncoder motorEncoders[] = new RelativeEncoder[3];
    public double kP, kI, kD, kIz, kFF, kMaxOutput, kMinOutput;
    private CANSparkMax.ControlType velocityMode = CANSparkMax.ControlType.kVelocity;


     /**
     * PIDController objects are commanded to a set point using the 
     * SetReference() method.
     * 
     * The first parameter is the value of the set point, whose units vary
     * depending on the control type set in the second parameter.
     * 
     * The second parameter is the control type can be set to one of four 
     * parameters:
     *  com.revrobotics.CANSparkMax.ControlType.kDutyCycle
     *  com.revrobotics.CANSparkMax.ControlType.kPosition
     *  com.revrobotics.CANSparkMax.ControlType.kVelocity
     *  com.revrobotics.CANSparkMax.ControlType.kVoltage
     */
    


    /**Initialize Shooter */
    public void init() {
        // PID coefficients
        kP = 10e-5; 
        kI = 0;
        kD = 0; 
        kIz = 0; 
        kFF = 0.000015; 
        kMaxOutput = 1; 
        kMinOutput = -1;
        
        for (int i = 0; i < shootMotors.length; i++ ) {
            shootPIDs[i] = shootMotors[i].getPIDController();
            motorEncoders[i] = shootMotors[i].getEncoder();
            setPID(kP, kI, kD, kIz, kFF, kMaxOutput, kMinOutput); // set PID coefficients
        }
        // display PID coefficients on SmartDashboard
        SmartDashboard.putNumber("P Gain", kP);
        SmartDashboard.putNumber("I Gain", kI);
        SmartDashboard.putNumber("D Gain", kD);
        SmartDashboard.putNumber("I Zone", kIz);
        SmartDashboard.putNumber("Feed Forward", kFF);
        SmartDashboard.putNumber("Max Output", kMaxOutput);
        SmartDashboard.putNumber("Min Output", kMinOutput);
    }

    /**Sets the speed of all three motors (for usage with percent power control mode) */
    public void setPercentPower(double pwr) {
        for (int i = 0; i < shootMotors.length; i++) {
            shootMotors[i].set(pwr);
        }
    } 

    /**Set a specific motor at a percent power */
    public void setPercentPower(double pwr, int ind) {
        System.out.println(ind > shootMotors.length ? "Out of Bounds Value" : "Good value");
        shootMotors[ind].set(pwr);
    } 

    /**Get speeds of all motors from the encoders and post them to SmartDashboard */
    public void getAllSpeeds() {
        for (int i = 0; i < motorEncoders.length; i++) {
            SmartDashboard.putNumber(("Motor " +  i), motorEncoders[i].getVelocity());
        }
    }

    /**Get speeds of motor of selected index from its encoder and post it to SmartDashboard */
    public double getSpeed(int motorIndex) {
        if (motorIndex <= 2) {
            return motorEncoders[motorIndex].getVelocity();
        } else {
            return -1;
        }
    }

    /**Shooter mode for what to do upon being given to command to pick a ball up */
    public void pickBallUp() {
        // setPercentPower(0.05, 1);
        shootPIDs[2].setReference(0.1 * MAX_SHOOT_VELOCITY, velocityMode);
    }

    /**Sets the velocity of each of the shooter motors using PID controls */
    public void setVelocity(double setPoint) {
        for (int i = 0; i < motorEncoders.length; i++) {
        shootPIDs[i].setReference(setPoint, velocityMode);
        }
    }

    /**Sets the PID coefficients for each of the shooter motors */
    public void setPID(double kP, double kI, double kD, double kIz, double kFF, double kMinOutput, double kMaxOutput) {
        for(int i = 0; i < shootPIDs.length; i++){
            shootPIDs[i].setP(kP);
            shootPIDs[i].setI(kI);
            shootPIDs[i].setD(kD);
            shootPIDs[i].setIZone(kIz);
            shootPIDs[i].setFF(kFF);
            shootPIDs[i].setOutputRange(kMinOutput, kMaxOutput);
        }
    }

    /**Get PID Coefficients from the Smart Dashboard entered on-the-fly */
    public void setPIDFromSmartDashboard () {
        double newP, newI, newD, newIZone, newFF, newMaxOuput, newMinOutput;
        newP = SmartDashboard.getNumber("P Gain", kP);
        newI = SmartDashboard.getNumber("I Gain", kI);
        newD = SmartDashboard.getNumber("D Gain", kD);
        newIZone = SmartDashboard.getNumber("I Zone", kIz);
        newFF = SmartDashboard.getNumber("Feed Forward", kFF);
        newMaxOuput = SmartDashboard.getNumber("Max Output", kMaxOutput);
        newMinOutput = SmartDashboard.getNumber("Min Output", kMinOutput);
        if(newP != kP || newI!= kI || newD!= kD || newIZone!= kIz || newFF !=kFF || newMaxOuput!=kMaxOutput || newMinOutput!=kMinOutput){
            setPID(newP, newI, newD, newIZone, newFF, newMaxOuput, newMinOutput);
        }
    }

    /**What the shooter does and checks for periodically */
    public void shooterPeriodic() {        
        getAllSpeeds();
        setPIDFromSmartDashboard();
        if(IO.aButtonIsPressed()) {
            pickBallUp();
        }
        if(IO.bButtonIsPressed()){
            setVelocity(IO.getRightXAxis() * MAX_SHOOT_VELOCITY);            
        }
    }
}
