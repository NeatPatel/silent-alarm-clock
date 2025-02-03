import { useState } from "react";
import { Permission, PermissionsAndroid,Platform, Pressable } from "react-native";
import { BleManager, Device } from "react-native-ble-plx";
import * as ExpoDevice from "expo-device";

// import base64 from "react-native-base64";

type PermissionCallback = (result:boolean)=>void;
const bleManager = new BleManager();

interface BluetoothLowEnergyApi{
    requestPermissions(): Promise<boolean>;
    scanForDevices(): void;
    allDevices: Device[];
}


const SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
const STEP_DATA_CHAR_UUID = "beefcafe-36e1-4688-b7f5-00000000000b";


export default function useBLE(): BluetoothLowEnergyApi{
    const [allDevices,setAllDevices] = useState<Device[]>([]);
    const [connectedDevice, setConnectedDevice] = useState<Device | null>(null);
    const requestAndroid31Permissions = async () => {
        const bluetoothScanPermission = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
        {
            title: "Location Permission",
            message: "Bluetooth Low Energy requires Location",
            buttonPositive: "OK",
        }
        );
        const bluetoothConnectPermission = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
        {
            title: "Location Permission",
            message: "Bluetooth Low Energy requires Location",
            buttonPositive: "OK",
        }
        );
        const fineLocationPermission = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
        {
            title: "Location Permission",
            message: "Bluetooth Low Energy requires Location",
            buttonPositive: "OK",
        }
        );

        return (
        bluetoothScanPermission === "granted" &&
        bluetoothConnectPermission === "granted" &&
        fineLocationPermission === "granted"
        );
    };

    const requestPermissions = async () =>{
        if (Platform.OS === "android") {
            if ((ExpoDevice.platformApiLevel ?? -1) < 31) {
                const granted = await PermissionsAndroid.request(
                PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
                {
                    title: "Location Permission",
                    message: "Bluetooth Low Energy requires Location",
                    buttonPositive: "OK",
                }
                );
                return granted === PermissionsAndroid.RESULTS.GRANTED;
            } else {
                const isAndroid31PermissionsGranted =
                await requestAndroid31Permissions();

                return isAndroid31PermissionsGranted;
            }
            } 
        else {
            return true;
            }

    };
    const isDuplicateDevice = (devices: Device[], nextDevice: Device)=>
        devices.findIndex(device => nextDevice.id === device.id)>-1;

    const scanForDevices = () =>{
        bleManager.startDeviceScan(null,null,(error,device)=>{
            if(error){
                console.log(error);
            }
            if(device && device.name === "Step-Sense"){
                bleManager.stopDeviceScan();
                setAllDevices(prevState =>{
                    if(!isDuplicateDevice(prevState,device)){
                        return[...prevState,device];
                    }
                    return prevState;
                });

            }
        });
    };
    // const onHeartRateUpdate = (
    //     error: BleError | null,
    //     characteristic: Characteristic | null
    // ) => {
    //     if (error) {
    //     console.log(error);
    //     return -1;
    //     } else if (!characteristic?.value) {
    //     console.log("No Data was recieved");
    //     return -1;
    //     }

    //     const rawData = base64.decode(characteristic.value);
    //     let innerHeartRate: number = -1;

    //     const firstBitValue: number = Number(rawData) & 0x01;

    //     if (firstBitValue === 0) {
    //     innerHeartRate = rawData[1].charCodeAt(0);
    //     } else {
    //     innerHeartRate =
    //         Number(rawData[1].charCodeAt(0) << 8) +
    //         Number(rawData[2].charCodeAt(2));
    //     }

    // };   

    // const connectToDevice = async (device: Device) => {
    //     try {
    //     const deviceConnection = await bleManager.connectToDevice(device.id);
    //     setConnectedDevice(deviceConnection);
    //     await deviceConnection.discoverAllServicesAndCharacteristics();
    //     bleManager.stopDeviceScan();
    //     startStreamingData(deviceConnection);
    //     } catch (e) {
    //     console.log("FAILED TO CONNECT", e);
    //     }
    // };

    // const startStreamingData = async (device: Device) => {
    //     if (device) {
    //     device.monitorCharacteristicForService(
    //         SERVICE_UUID,
    //         STEP_DATA_CHAR_UUID,
    //         onHeartRateUpdate
    //     );
    //     } else {
    //     console.log("No Device Connected");
    //     }
    // };

    return {
        requestPermissions,
        scanForDevices,
        allDevices,
    };
    }

