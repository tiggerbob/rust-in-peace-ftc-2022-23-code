package org.firstinspires.ftc.teamcode.teleops;

import static org.firstinspires.ftc.teamcode.configs.HardwareNames.backLeftMotorName;
import static org.firstinspires.ftc.teamcode.configs.HardwareNames.backRightMotorName;
import static org.firstinspires.ftc.teamcode.configs.HardwareNames.frontLeftMotorName;
import static org.firstinspires.ftc.teamcode.configs.HardwareNames.frontRightMotorName;
import static org.firstinspires.ftc.teamcode.configs.HardwareNames.grabServo1Name;
import static org.firstinspires.ftc.teamcode.configs.HardwareNames.spoolMotorName;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.configs.PoseStorage;
import org.firstinspires.ftc.teamcode.roadRunner.drive.SampleMecanumDrive;

@TeleOp
@Config

public class DualControllerDriveTeleOp extends OpMode {

    //drive stuff
    private DcMotor motorFR, motorFL, motorRR, motorRL;
    private double powerFR, powerFL, powerRR, powerRL;
    private double drive = 0, strafe = 0, turn = 0; //these values only exist to be overridden later
    private double speed = 1; //this value only exist to be overridden later
    private boolean lockSpeed = true;
    public static  double constantSpeedMult = 0.5;
    public static  double constantSpeedMultChangeMult = 0.25;
    private boolean wasPressingDpadUp = false, wasPressingDpadDown = false;

    //spool stuff
    private DcMotor spoolMotor, armTwo;
    public static double spoolPower = 0.5;
    public static  double armTwoPos = 0;
    public static  double spoolSpeedMultiplier = 0.5;

    //servo stuff
    private CRServo grabServo;
    private double servoSpeed = 1; //this value only exist to be overridden later
    public static double servoSpeedMultiplier = 0.5;

    //extra
    private boolean wasPressingA, wasPressingB, wasPressingX, wasPressingY;

    //roadrunner implementation
    private SampleMecanumDrive roadrunnerDriver;
    private Pose2d currentPose;



    @Override
    public void loop() {
        takeControllerInput();

        //drive();
        roadrunnerDrive();
        moveArm();

        armGrab();

        telemetry.update();
    }

    private void roadrunnerDrive() {

        Vector2d input = new Vector2d(
                drive,
                -strafe
        );
        //input = input.rotated(-currentPose.getHeading());
        /** Pass in the rotated input + right stick value for rotation
         * Rotation is not part of the rotated input thus must be passed in separately
         */
        roadrunnerDriver.setWeightedDrivePower(
                new Pose2d(
                        input.getX(),
                        input.getY(),
                        -turn
                )
        );

        roadrunnerDriver.update();

        currentPose = roadrunnerDriver.getPoseEstimate();


        telemetry.addData("x", currentPose.getX());
        telemetry.addData("y", currentPose.getY());
        telemetry.addData("heading", currentPose.getHeading());

    }

    private void takeControllerInput(){
        /**
         * reads all of the controller inputs
         */
        drive = -1*gamepad1.left_stick_y;
        strafe = gamepad1.left_stick_x;
        turn = gamepad1.right_stick_x;

        /**
         * deprecated code, exists here for posterity and in case of
         * request for reimplementation
         *
        //if A is pressed, it will unlock more variable speed control, else will run at constantSpeedMult
        if (gamepad1.a) {
            if (!wasPressingA) {
                lockSpeed = !lockSpeed;
            }
            wasPressingA = true;
        }
        else wasPressingA = false;
        */

        //every time the button is pressed, changes the speed multiplier by constantSpeedMultChangeMult
        if (gamepad1.dpad_up) {
            if (!wasPressingDpadUp) {
                constantSpeedMult = Math.min(constantSpeedMult+constantSpeedMultChangeMult, 1);
            }
            wasPressingDpadUp = true;
        }
        else wasPressingDpadUp = false;

        if (gamepad1.dpad_down) {
            if (!wasPressingDpadDown) {
                constantSpeedMult = Math.max(constantSpeedMult-constantSpeedMultChangeMult, 0);
            }
            wasPressingDpadDown = true;
        }
        else wasPressingDpadDown = false;


        telemetry.addData("Constant Speed Mult", constantSpeedMult);

        //sets the speed
        speed = lockSpeed ? constantSpeedMult : gamepad1.right_trigger;

        // sets them to gamepad2
        // **WARNING POWER VARIABLE**
        spoolPower = gamepad2.left_stick_y;
        servoSpeed = gamepad2.right_stick_y;

    }

    /**
     * @deprecated use roadRunnerDrive
     * here for posterity only
     */
    private void drive(){

        powerFR = drive - strafe;
        powerFL = drive + strafe;
        powerRR = drive + strafe;
        powerRL = drive - strafe;

        addTurn(turn);

        // multiplies by speed
        powerFR *= speed;
        powerFL *= speed;
        powerRR *= speed;
        powerRL *= speed;

        // applies the power
        motorFR.setPower(powerFR);
        motorFL.setPower(powerFL);
        motorRR.setPower(powerRR);
        motorRL.setPower(powerRL);

        telemetry.addData("powerFR", powerFR);

    }

    /**
     * @deprecated use roadRunnerDrive
     * here for posterity only
     */
    private void addTurn(double turn){
        powerFR -= turn;
        powerRR -= turn;
        powerFL += turn;
        powerRL += turn;
    }


    private void moveArm(){

        spoolMotor.setPower(spoolPower * spoolSpeedMultiplier);
        telemetry.addData("spoolMotor Position", spoolMotor.getCurrentPosition());
    }


    /**
     * moves the linear slide with a pid controller that is hopefully correctly implemented
     */
    private LinearSlideDriver slideDriver;
    private void moveArmWithPID(int target){
        slideDriver.setTarget(target);
        int[] slidePIDOutput = slideDriver.run();
        telemetry.addData("Slide Target", slidePIDOutput[0]);
        telemetry.addData("Slide Current", slidePIDOutput[1]);
        telemetry.addData("Slide Error", slidePIDOutput[2]);
    }



    private void armGrab(){
        grabServo.setPower(servoSpeed * servoSpeedMultiplier);
        //telemetry.addData("Servo Position", grabServo.get);
    }


    @Override
    public void init() {
        /**
         * initializes the drive motors
         */
        motorFR = hardwareMap.get(DcMotor.class, frontRightMotorName) ;
        motorFL = hardwareMap.get(DcMotor.class, frontLeftMotorName);
        motorRR = hardwareMap.get(DcMotor.class, backRightMotorName);
        motorRL = hardwareMap.get(DcMotor.class, backLeftMotorName);

        motorFL.setDirection(DcMotorSimple.Direction.REVERSE);
        motorRL.setDirection(DcMotorSimple.Direction.REVERSE);

        motorFR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorFL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorRR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorRL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        motorFR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorRR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorRL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        motorFR.setPower(0);
        motorFL.setPower(0);
        motorRR.setPower(0);
        motorRL.setPower(0);

        /**
         * initializes the arm motors and servos
         * @deprecated use runMotorWithPID() instead
         * exists to keep functionality of older code
         */
        spoolMotor = hardwareMap.get(DcMotor.class, spoolMotorName);
        spoolMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        spoolMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        spoolMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        spoolMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        /**
         * initalizes the spoolMotor's pid controller stuff
         */
        slideDriver = new LinearSlideDriver(hardwareMap);

        //TODO: TEMPORARY FIX TO NOT THROW ERROR
        grabServo = hardwareMap.get(CRServo.class, grabServo1Name);

        /**
         * initializes the roadrunner stuff
         */
        roadrunnerDriver = new SampleMecanumDrive(hardwareMap);
        currentPose = PoseStorage.currentPose;
        roadrunnerDriver.setPoseEstimate(currentPose);

    }
}
