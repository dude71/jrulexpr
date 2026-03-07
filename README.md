# JRuleXpr

JRuleXpr (JRule expressions) is a small JRule automation extension for creating [JRule](https://github.com/seaside1/jrule) rules from Openhab item expressions. It is well suited for sensor based home automation. It removes the need to manually write trigger based code.

JRuleXpr will generate JRule rules from expressions specified in metadata of Openhab items. The expressions are evaluated by [EvalEx](https://github.com/ezylang/EvalEx).

The JRuleXpr rule generator will scan all Openhab items at Openhab startup for jrx(..) metadata and generate .java rule files for them. These will be compiled by JRule. These java rule files are stored in JRule "rules-user" folder. Changes to the jrx(..) metadata <ins>needs regeneration</ins> of the java rules file(s) of the changed items. This can be triggered by the manual deletion of the .java and .class files in the "rules-user" folder.

## Installation

1. Install JRule. Copy the latest [JRule 4](https://github.com/seaside1/jrule/releases) release to openhab-addons folder.
2. Copy jrulexpr-x.y.z-lib.jar to ${OPENHAB_CONF}/automation/jrule/ext-lib folder.
3. Copy jrulexpr-x.y.z-rulegen.jar to ${OPENHAB_CONF}/automation/jrule/rules-jar folder.
4. (re)start Openhab

## Configuration (environment vars)

* JRX_STARTUP_WAIT number of milliseconds to wait before generating rules at Openhab startup. Default 5000.
* JRX_RULE_WAIT number of milliseconds to wait per generated rule before setting loaded state (item NR_JRX_LOADED) is set to 1. Default 50.
* JRX_LOADED_WAIT number of milliseconds to wait after generating rules before setting loaded state to 1. Default 2000.

## Usage

These are the metadata fields that JRuleXpr uses:

* **jrxc**   Configuration: ruleClass, cron, forceCmd, noTrigger, skipJrxf
* **jrxp**   Pre eval: boolean expression which is evaluated when rule is triggered. When false it will not continue.
* **jrx**    Main eval: boolean expression which is evaluated after jrxp passes.
* **jrxt**   JRule value sent to item when jrx is true.
* **jrxf**   JRule value sent to item when jrx is false.
* **jrxv_**  variables/subexpressions to be used in in jrxp, jrx, jrxt, jrxf

Inside expressions [EvalEx functions](https://ezylang.github.io/EvalEx/references/functions.html) and JRuleXpr functions can be used. JRuleXpr functions extend the core functionality and can make the jrx(..) expressions more powerful.

## Jrxc

* **ruleClass**  The name of the class where the item rule will be added as a method. Default is the item's JRule type suffixed with "Rules". Example: NumberRules. The method name is the item name in lowerCamelCase. Example: ruleClass=AlarmRules.
* **cron** A cron expression used to trigger the item rule. Item rules will be automatically triggered when the HOUR or MINUTE functions are used inside their jrxp or jrx metadata (every hour or minute). If these functions are not used or an extra cron is needed a jrxc cron expression can be used.
* **forceCmd** Force state change of the item. Default behaviour is to not update the items state when the rule is triggered and no new state is evaluated by jrxt or jrxf.
* **noTrigger** List of items that should not trigger the item rule. By default any item specified in jrxp or jrx will trigger the item rule on change. Example: noTrigger='NR_ALARM_ARMED, NR_ALARM_STAY'.
* **skipJrxf** Do not evaluate jrxf and do not update item state when jrx evaluates to false.

## Generated Java classes

All generated JRule item methods call just one method:

````
execRule(String itemName, JRuleEvent event);
````

The JRule method annotations (@JRuleWhenItemChange and @JRuleWhenCronTrigger) and the itemName differ per item method. All rule logic is handled within the JRuleXpr code. Item rules with the same jrxc ruleClass are put in the same Java class. Use the same ruleClass for items with a common theme. Example "TemperatureRules" for thermostats, heaters and temperature sensors. 

## Example 1

```
Number NR_AMBIENT_LIGHT_NEED "Ambient light need [%d]" (HomeLightingStates) {
    jrxc='ruleClass=LightingRules',
    jrxp='NR_SYSTEM == 1',
    jrx="COALESCE(NR_IS_DARK, 0) == 1 && HOUR() > 15 && HOUR() < 23" 
}

Number NR_KITCHEN_LIGHT_NEED "Kitchen light need [%d]" (GroundFloorLightingStates) {
    jrxc='ruleClass=LightingRules',
    jrx="NR_LIGHTING_NEED == 1 && NR_KITCHEN_PRESENCE == 1"
}

Switch SW_KITCHEN_LEDSTRIP "Switch" <switch> (Lighting, KitchenRgbStrip) {
    channel="mqtt:topic:ha:tuya_led_ctrl_ts0504b:switch",
    jrxc='ruleClass=LightingRules',
    jrxp='ENABLED("SW_AUTO_LIGHTING", "SW_AUTO_LIGHTING_OVERRIDE")',
    jrx='(NR_AMBIENT_LIGHT_NEED == 1 && NR_HOME_PROX == 0) || NR_KITCHEN_LIGHT_NEED == 1'
}
```
In example 1 the kitchen ledstrip is switched on when auto lighting is enabled and there is ambient light need while no one is home or there is need for lighting in kitchen (presence: motion sensor is tripped). For simplicity sake not all Openhab items involved are shown. The idea is that one state (NR_IS_DARK) influences another state (NR_LIGHTING_NEED) which in turn influences the state of the ledstrip. The ledstrip item does not specify a jrxt or jrxf. These are optional since they have a default value based on the Openhab item type. For a switch the jrxt default is ON and jrxf default is OFF.

## Example 2

````
Number NR_GAS_USAGE "Gas usage [%.2f m3]" <qualityofservice> (Gas, MeterboxGasMetrics) {
    channel="dsmr:m3_v5_0:meter:gas:m3meter_value",
    jrxc='ruleClass=GasRules',
    jrxp='INITIAL("NR_GAS_USAGE") == NULL',
    jrxt='NR_GAS_USAGE'
}

Number NR_GAS_USAGE_TODAY "Gas usage today [%.2f m3]" <fire> (Gas, MeterboxGasMetrics) {
    jrxc='ruleClass=GasRules',
    jrxp='NR_GAS_USAGE != NULL && INITIAL("NR_GAS_USAGE") != NULL',
    jrxt='NR_GAS_USAGE - INITIAL("NR_GAS_USAGE")'
}
````
In example 2 the use of the JRuleXpr INITIAL function is demonstrated. The gas usage item gets an initial value tag when first triggered. Items specified in jrxp or jrx expressions will trigger the generated rule on state changes. In this case a change in NR_GAS_USAGE will trigger the rule. If no initial value tag is present (jrxp expression) the jrx is evaluated. The default value for jrx is true thus jrxt is evaluated (no change to gas usage). The gas usage today item calculates the difference between the current (total gas usage from DSMR meter) and the initial value. This example assumes that the Openhab system is rebooted every night otherwise it would calculate the gas usage since first started.

## Example 3

````
Number NR_FRONTDOOR_MOTION "Motion frontdoor" (InMemory, Driveway) {
    jrxc="ruleClass=MotionRules",
    jrxp='MINTIME(60)',
    jrx='NR_ZONE_FRONTDOOR_FAULT == 1 || NR_DOORBELL_MOTION == 1'
}
````
In example 3 the use of the JRuleXpr MINTIME function is demonstrated. This function <ins>MUST ONLY BE CALLED FROM jrxp</ins> ! When first called (with seconds as parameter) it evaluates to true and internally starts a timer for the specified # of seconds (if jrx evaluates to true). When the generated rule is not triggered anymore the timer will timeout and evaluate the jrx again. If jrx is still/again true it will reschedule the timer and not change the item state. If jrx is false the timer is cleared and the state is set to jrxf evaluation value. If the generated rule is triggered before timeout the internal timer is rescheduled as well. This function makes sure that the initial jrxt state is kept for <ins>AT LEAST X NUMBER OF SECONDS</ins>. This can be used for motion sensors to keep motion detected state for some time even when the sensor already flipped to 'no motion'.


