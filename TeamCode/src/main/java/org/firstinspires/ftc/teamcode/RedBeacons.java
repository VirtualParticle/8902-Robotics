package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.main;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by Mark on 2/21/2017.
 */
@Autonomous(name = "Red Beacon", group = "default")
public class RedBeacons extends LinearOpMode {

    private ColorSensor colorSensor;
    private OpticalDistanceSensor ods;

    private Servo colorServo;
    private Servo odsServo;

    private DcMotor backLeft;
    private DcMotor backRight;
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private ArrayList<DcMotor> motors = new ArrayList<>();

    //Variables
    private boolean pushed = false;
    private int direction;

    //Sensor vars
    private double r;
    private double g;
    private double b;
    private double distance;

    //Checks using the ODS for a wall
    public boolean wall() {

        if (ods.getLightDetected() >= 0.05) {
            return true;
        }
        else {
            return false;
        }

    }

    //Checks for the correct beacon color
    public boolean color() {

        double r = colorSensor.red();
        double b = colorSensor.blue();

        if (r >= 3 && r > b) {
            return true;
        }
        else {
            return false;
        }

    }

    //Checks if the color sensor returns nothing
    public boolean noColor() {

        int r = colorSensor.red();
        int g = colorSensor.green();
        int b = colorSensor.blue();

        if (r + g + b == 0) {
            return true;
        }
        else {
            return false;
        }

    }

    //Pushes beacon
    public void push() {

        sensors();

        if (pushed == false) {
            pushed = true;
            resetStartTime();
        }

        //Moves forward for 0.5 seconds
        main.move(0, 0.1, motors);
        sleep(500);

        //Moves back until wall no longer detected
        while (wall()) {
            sensors();
            main.move(1, 0.1, motors);
        }

        //Moves forward until wall detected, plus 0.1 seconds
        while (!wall()) {
            sensors();
            main.move(0, 0.1, motors);
        }
        sleep(100);
        main.move(0, 0, motors);


    }

    //Acquires Sensor data
    public void sensors() {

        distance = ods.getLightDetected();
        r = colorSensor.red();
        g = colorSensor.green();
        b = colorSensor.blue();

    }

    public void goRightForBeacon() {

        while (!color() && noColor()) {
            sensors();

            if (color()) {
                push();
            }

            main.move(3, 0.1, motors);
        }

    }

    public void runOpMode() {

        //Initialization Routine

        //Initializes Motors
        backLeft = hardwareMap.dcMotor.get("backLeft");
        backRight = hardwareMap.dcMotor.get("backRight");
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");

        //Initializes Servos
        colorServo = hardwareMap.servo.get("colorServo");
        odsServo = hardwareMap.servo.get("odsServo");

        //Initializes Sensors
        colorSensor = hardwareMap.colorSensor.get("colorSensor");
        ods = hardwareMap.opticalDistanceSensor.get("distanceSensor");

        //Adds all motors to a list
        motors.add(backLeft);
        motors.add(backRight);
        motors.add(frontLeft);
        motors.add(frontRight);

        //Turns off color sensor light
        colorSensor.enableLed(false);

        //Makes sure the sensors are in the right spot
        colorServo.setPosition(1.0);
        odsServo.setPosition(1.0);

        resetStartTime();
        waitForStart();

        //Running Code
        while (opModeIsActive()) {

            //Acquires Sensor data
            sensors();

            //Starts from starting position, finds wall
            main.move(0, 1, motors);
            sleep(1300);
            main.turn(0, 1, motors);
            sleep(475);
            main.move(0, 1, motors);
            sleep(450);
            while (!wall()) {
                sensors();
                main.move(0, 0.1, motors);
            }

            resetStartTime();
            //Wall found, moves left until beacon is found, and pushes if it is the right color

            while (!color() && noColor()) {
                sensors();

                if (color()) {
                    push();
                }
                else if (!wall()) {
                    main.move(0, 0.1, motors);
                }
                else {
                    if (getRuntime() > 1) {
                        main.move(3, 0.1, motors);
                        direction = 1;
                    }
                    else {
                        main.move(2, 0.1, motors);
                        direction = 0;
                    }
                }
            }

            //If the beacon hasn't been pushed, but the robot has found the beacon
            if (!pushed) {
                while (!color()) {
                    sensors();

                    if (color()) {
                        push();
                    }

                    //Continues moving in the direction that it was prior
                    if (direction == 0 && !pushed) {
                        main.move(2, 0.1, motors);
                    }
                    if (direction == 1 && !pushed) {
                        main.move(3, 0.1, motors);
                    }

                }
            }

            //Resets, and prepares to push the next beacon
            pushed = false;
            main.move(3, 0.5, motors);
            sleep(300);

            //Moves to the right, staying the correct distance from the wall, and presses the beacon when it is in front of the correct color
            goRightForBeacon();

            //Turns the robot to go for the next two beacons
            main.turn(1, 1, motors);
            sleep(475);
            main.turn(0, 0, motors);

            //Makes sure 10 seconds have passed
            while (getRuntime() <= 7.5) {
                return;
            }

            //Stores current time to know how long the robot must travel to come back
            double time = getRuntime();

            //Ready to continue. Goes for first beacon on the right.
            goRightForBeacon();

            //Goes for second beacon on the right
            goRightForBeacon();

            double time2 = (getRuntime() - time) * 100;

            long timeL = (long) time2;

            //Comes back to where robot was before previous two beacons
            main.move(2, 1, motors);
            sleep(timeL);

            //Moves back to near the first beacon
            main.move(1, 1, motors);
            sleep((long) (time2*0.75));

            //Move in front of the center goal
            main.move(3, 1, motors);
            sleep((long) (time2/2));

            //Move the ball and park on the goal
            main.move(0, 1, motors);
            sleep(100);
            main.move(0, 0, motors);

        }
    }

}