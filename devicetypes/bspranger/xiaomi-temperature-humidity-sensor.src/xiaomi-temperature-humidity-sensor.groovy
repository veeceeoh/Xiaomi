/**
 *  Copyright 2017 bspranger
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  2017-03 First release of the Xiaomi Temp/Humidity Device Handler
 *  2017-03 Includes battery level (hope it works, I've only had access to a device for a limited period, time will tell!)
 *  2017-03 Last checkin activity to help monitor health of device and multiattribute tile
 *  2017-03 Changed temperature to update on .1° changes - much more useful
 *  2017-03-08 Changed the way the battery level is being measured. Very different to other Xiaomi sensors.
 *  2017-03-23 Added Fahrenheit support
 *  2017-03-25 Minor update to display unknown battery as "--", added fahrenheit colours to main and device tiles
 *  2017-03-29 Temperature offset preference added to handler
 *
 *  known issue: these devices do not seem to respond to refresh requests left in place in case things change
 *  known issue: tile formatting on ios and android devices vary a little due to smartthings app - again, nothing I can do about this
 *  known issue: there's nothing I can do about the pairing process with smartthings. it is indeed non standard, please refer to community forum for details
 *  bspranger - renamed to bspranger to remove confusion of a4refillpad
 *  bspranger - rewritting the DH to use Maps so it conforms with the other Xiaomi DHs.
 */

metadata {
    definition (name: "Xiaomi Temperature Humidity Sensor", namespace: "bspranger", author: "bspranger") {
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Sensor"
        capability "Battery"
        capability "Health Check"
        
        attribute "lastCheckin", "String"
	attribute "lastCheckinDate", "String"
        attribute "batteryRuntime", "String"
        
        fingerprint profileId: "0104", deviceId: "0302", inClusters: "0000,0001,0003,0009,0402,0405"

        command "resetBatteryRuntime"
}

    // simulator metadata
    simulator {
        for (int i = 0; i <= 100; i += 10) {
            status "${i}F": "temperature: $i F"
        }

        for (int i = 0; i <= 100; i += 10) {
            status "${i}%": "humidity: ${i}%"
        }
    }
    
    preferences {
        section {
            input title: "Temperature Offset", description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter '-5'. If 3 degrees too cold, enter '+3'. Please note, any changes will take effect only on the NEXT temperature change.", displayDuringSetup: false, type: "paragraph", element: "paragraph"
            input "tempOffset", "number", title: "Degrees", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: true, required: true, defaultValue: 0
        }
	section {
            input title:"Humidity Offset", description:"This feature allows you to correct any humidity variations by selecting an offset. Ex: If your sensor consistently reports a humidity that's 5 too high, you'd enter '-5'. If 3 too low, enter '+3'. Please note, any changes will take effect only on the NEXT humidity change.", displayDuringSetup: true, type: "paragraph", element:"paragraph"
            input "humidOffset", "number", title:"Humidity", description:"Adjust humidity by this many units", range: "*..*", displayDuringSetup: true, required: true, defaultValue: 0
        }
        section {
		input name: "dateformat", type: "enum", title: "Set Date Format\n US (MDY) - UK (DMY) - Other (YMD)", description: "Date Format", required: false, options:["US","UK","Other"]
		input description: "Only change the settings below if you know what you're doing", displayDuringSetup: false, type: "paragraph", element: "paragraph", title: "ADVANCED SETTINGS"
		input name: "voltsmax", title: "Max Volts\nA battery is at 100% at __ volts\nRange 2.8 to 3.4", type: "decimal", range: "2.8..3.4", defaultValue: 3, required: false
		input name: "voltsmin", title: "Min Volts\nA battery is at 0% (needs replacing) at __ volts\nRange 2.0 to 2.7", type: "decimal", range: "2..2.7", defaultValue: 2.5, required: false
        }
	
    }
    
    // UI tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"temperature", type:"generic", width:6, height:4) {
            tileAttribute("device.temperature", key:"PRIMARY_CONTROL"){
                attributeState("temperature", label:'${currentValue}°',
                backgroundColors:[
                    [value: 0, color: "#153591"],
                    [value: 5, color: "#1e9cbb"],
                    [value: 10, color: "#90d2a7"],
                    [value: 15, color: "#44b621"],
                    [value: 20, color: "#f1d801"],
                    [value: 25, color: "#d04e00"],
                    [value: 30, color: "#bc2323"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]                                      
                ]
            )
            }
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'Last Update: ${currentValue}', icon: "st.Health & Wellness.health9")
            }
        }
        standardTile("humidity", "device.humidity", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:'${currentValue}%', icon:"st.Weather.weather12"
        }
        
        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
            state "default", label:'${currentValue}%', unit:"",
            backgroundColors:[
                [value: 0, color: "#c0392b"],
                [value: 25, color: "#f1c40f"],
                [value: 50, color: "#e67e22"],
                [value: 75, color: "#27ae60"]
            ]
        }
        
        valueTile("temperature2", "device.temperature", decoration: "flat", inactiveLabel: false) {
            state "temperature", label:'${currentValue}°', icon: "st.Weather.weather2",
                backgroundColors:[
                    [value: 0, color: "#153591"],
                    [value: 5, color: "#1e9cbb"],
                    [value: 10, color: "#90d2a7"],
                    [value: 15, color: "#44b621"],
                    [value: 20, color: "#f1d801"],
                    [value: 25, color: "#d04e00"],
                    [value: 30, color: "#bc2323"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]                                      
                ]
        }
            valueTile("batteryRuntime", "device.batteryRuntime", inactiveLabel: false, decoration: "flat", width: 6, height: 2) {
            state "batteryRuntime", label:'Battery Changed: ${currentValue} - Tap to reset Date', unit:"", action:"resetBatteryRuntime"
    }     
        main(["temperature2"])
        details(["temperature", "battery", "humidity","batteryRuntime"])
    }
}

// Parse incoming device messages to generate events
def parse(String description) {

    // send event for heartbeat
    def now = formatDate()    
    def nowDate = new Date(now).getTime()
    sendEvent(name: "lastCheckin", value: now)
    sendEvent(name: "lastCheckinDate", value: nowDate, displayed: false)

    Map map = [:]

    if (description?.startsWith("temperature: ")) {
        map = parseTemperature(description)
    } else if (description?.startsWith("humidity: ")) {
        map = parseHumidity(description)
    } else if (description?.startsWith('catchall:')) {
        map = parseCatchAllMessage(description)
    } else if (description?.startsWith('read attr - raw:')) {
        map = parseReadAttr(description)
    }

    log.debug "${device.displayName}: Parse returned ${map}"
    def results = map ? createEvent(map) : null
    return results
}


private Map parseTemperature(String description){
    def temp = ((description - "temperature: ").trim()) as Float 

    if (!(settings.tempOffset)){
        settings.tempOffset = 0
    }
    
    if (temp > 100)
    {
        temp = 100.0 - temp
    }
    
    if (getTemperatureScale() == "C") {
        if (settings.tempOffset) {
            temp = (Math.round(temp * 10))/ 10 + settings.tempOffset as Float
        } else {
            temp = (Math.round(temp * 10))/ 10 as Float
        }
    } else {
        if (settings.tempOffset) {
            temp = (Math.round((temp * 90.0)/5.0))/10.0 + 32.0 + settings.tempOffset as Float
        } else {
            temp = (Math.round((temp * 90.0)/5.0))/10.0 + 32.0 as Float
        }
    }
    def units = getTemperatureScale()
    def result = [
        name: 'temperature',
        value: temp,
        unit: units,
        isStateChange:true,
        descriptionText : "${device.displayName} temperature is ${temp}${units}"
    ]
    return result
}


private Map parseHumidity(String description){
    def pct = (description - "humidity: " - "%").trim()
        
    if (pct.isNumber()) {
        pct =  Math.round(new BigDecimal(pct))
        
	if (!(settings.humidOffset)){
        settings.humidOffset = 0
        }
	if (settings.humidOffset) {
            pct = (pct + settings.humidOffset)
            pct = pct.round(2);
        }
        def result = [
            name: 'humidity',
            value: pct,
            unit: "%",
            isStateChange:true,
            descriptionText : "${device.displayName} Humidity is ${pct}%"
        ]
        return result
    }
    
    return [:]
}


private Map parseCatchAllMessage(String description) {
    def i
    Map resultMap = [:]
    def cluster = zigbee.parse(description)
    log.debug cluster
    if (cluster) {
        switch(cluster.clusterId) 
        {
            case 0x0000:
                def MsgLength = cluster.data.size();

                // Original Xiaomi CatchAll does not have identifiers, first UINT16 is Battery
                if ((cluster.data.get(0) == 0x02) && (cluster.data.get(1) == 0xFF))
                {
                    for (i = 0; i < (MsgLength-3); i++)
                    {
                        if (cluster.data.get(i) == 0x21) // check the data ID and data type
                        {
                            // next two bytes are the battery voltage.
                            resultMap = getBatteryResult((cluster.data.get(i+2)<<8) + cluster.data.get(i+1))
                            break
                        }
                    }
                }else if ((cluster.data.get(0) == 0x01) && (cluster.data.get(1) == 0xFF))
                {
                    for (i = 0; i < (MsgLength-3); i++)
                    {
                        if ((cluster.data.get(i) == 0x01) && (cluster.data.get(i+1) == 0x21))  // check the data ID and data type
                        {
                            // next two bytes are the battery voltage.
                            resultMap = getBatteryResult((cluster.data.get(i+3)<<8) + cluster.data.get(i+2))
                            break
                        }
                    }
                }
            break
        }
    }
    return resultMap
}


// Parse raw data on reset button press to retrieve reported battery voltage
private Map parseReadAttr(String description) {
    Map resultMap = [:]

    def cluster = description.split(",").find {it.split(":")[0].trim() == "cluster"}?.split(":")[1].trim()
    def attrId = description.split(",").find {it.split(":")[0].trim() == "attrId"}?.split(":")[1].trim()
    def value = description.split(",").find {it.split(":")[0].trim() == "value"}?.split(":")[1].trim()
    def model = value.split("01FF")[0]
    def data = value.split("01FF")[1]

    if (cluster == "0000" && attrId == "0005")  {
        def modelName = ""
        // Parsing the model
        for (int i = 0; i < model.length(); i+=2) 
        {
            def str = model.substring(i, i+2);
            def NextChar = (char)Integer.parseInt(str, 16);
            modelName = modelName + NextChar
        }
        log.debug "${device.displayName} reported: cluster: ${cluster}, attrId: ${attrId}, value: ${value}, model:${modelName}, data:${data}"
    }
    if (data[4..7] == "0121") {
        resultMap = getBatteryResult(Integer.parseInt((data[10..11] + data[8..9]),16))
    }
    return resultMap
}


private Map getBatteryResult(rawValue) {
    def rawVolts = rawValue / 1000
	def minVolts
    def maxVolts

    if(voltsmin == null || voltsmin == "")
    	minVolts = 2.5
    else
   	minVolts = voltsmin
    
    if(voltsmax == null || voltsmax == "")
    	maxVolts = 3.0
    else
	maxVolts = voltsmax
    
    def pct = (rawVolts - minVolts) / (maxVolts - minVolts)
    def roundedPct = Math.min(100, Math.round(pct * 100))

    def result = [
        name: 'battery',
        value: roundedPct,
        unit: "%",
        isStateChange:true,
        descriptionText : "${device.displayName} raw battery is ${rawVolts}v"
    ]
    
    log.debug "${device.displayName}: ${result}"
    if (state.battery != result.value)
    {
        state.battery = result.value
        resetBatteryRuntime()
    }
    return result
}

def resetBatteryRuntime() {
    def now = formatDate(true)    
    sendEvent(name: "batteryRuntime", value: now)
}

def configure() {
    log.debug "${device.displayName}: configure"
    state.battery = 0
    checkIntervalEvent("configure");
}

def installed() {
    state.battery = 0
    checkIntervalEvent("installed");
}

def updated() {
    checkIntervalEvent("updated");
}

private checkIntervalEvent(text) {
    // Device wakes up every 1 hours, this interval allows us to miss one wakeup notification before marking offline
    log.debug "${device.displayName}: Configured health checkInterval when ${text}()"
    sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def formatDate(batteryReset) {
    if (dateformat == "US" || dateformat == "" || dateformat == null) {
        if (batteryReset)
            return new Date().format("MMM dd yyyy", location.timeZone)
        else
            return new Date().format("EEE MMM dd yyyy h:mm:ss a", location.timeZone)
    }
    else if (dateformat == "UK") {
        if (batteryReset)
            return new Date().format("dd MMM yyyy", location.timeZone)
        else
            return new Date().format("EEE dd MMM yyyy h:mm:ss a", location.timeZone)
        }
    else {
        if (batteryReset)
            return new Date().format("yyyy MMM dd", location.timeZone)
        else
            return new Date().format("EEE yyyy MMM dd h:mm:ss a", location.timeZone)
    }
}
