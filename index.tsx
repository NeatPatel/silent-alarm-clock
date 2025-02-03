import { Image, StyleSheet, Platform } from 'react-native';
import {
  SafeAreaView,
  Text,
  TouchableOpacity,
  View,
  TextInput,
} from 'react-native';
import { HelloWave } from '@/components/HelloWave';
import ParallaxScrollView from '@/components/ParallaxScrollView';
import { ThemedText } from '@/components/ThemedText';
import { ThemedView } from '@/components/ThemedView';

import { BleManager, Device } from "react-native-ble-plx";
import React, {useState,useRef,useEffect} from 'react';
// import DeviceModal from '@/DeviceConnectionModel';
import useBLE  from '@/useBLE';
import { atob } from "react-native-quick-base64";
import { FlatList } from 'react-native';

const SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
const STEP_DATA_CHAR_UUID = "beefcafe-36e1-4688-b7f5-00000000000b";
const ALARM_TIME_CHAR_UUID = "DAA2";
const bleManager = new BleManager();

export default function HomeScreen() {
  const [bleCount, setBleCount] = useState(0);
  const [serviceUUID, setServiceUUID] = useState('');
  const [characteristicUUID, setCharacteristicUUID] = useState('');
  const [deviceID, setDeviceID] = useState('empty id');
  const [allDevices, setAllDevices] = useState<Device[]>([]);
  
  const {
    requestPermissions,
    // scanForPeripherals,
    // allDevices,
    scanForDevices,
    // connectToDevice,
    // connectedDevice,
    // heartRate,
    // disconnectFromDevice,
  } = useBLE();
  const [isModalVisible, setIsModalVisible] = useState<boolean>(false);
  const [connectionStatus, setConnectionStatus] = useState("Searching...");
  const [Count, setCount] = useState(0);
  const deviceRef = useRef(null);
  const [isScanning, setIsScanning] = useState(false);
  const [alarmTime, setAlarmTime] = useState('');

  const scanAndConnectDevices = async () => {
    const isPermissionEnabled = await requestPermissions();
    if (isPermissionEnabled)
      {
        bleManager.startDeviceScan(null,null,(error,device) =>{
          if (error){
            console.error(error);
            setConnectionStatus("error for searching device...");
            return;
          }
          if (device.name === 'Step-Sense')
          {
            bleManager.stopDeviceScan();
            setConnectionStatus("Connecting...");
            connectToDevice(device);
          }
        });

        
      } 
  };

  const scanForAvailableDevices = async () => {
    const isPermissionEnabled = await requestPermissions();
    if (isPermissionEnabled) {
      setIsScanning(true);
      setAllDevices([]);
      bleManager.startDeviceScan(null, null, (error, device) => {
        if (error) {
          console.error(error);
          setConnectionStatus("Error scanning for devices");
          setIsScanning(false);
          return;
        }
        if (device && device.name) {
          setAllDevices((prevDevices) => {
            if (!prevDevices.some((d) => d.id === device.id)) {
              return [...prevDevices, device];
            }
            return prevDevices;
          });
        }
      });

      // Stop scanning after 10 seconds
      setTimeout(() => {
        bleManager.stopDeviceScan();
        setIsScanning(false);
      }, 10000);
    }
  };

  const renderDeviceItem = ({ item }) => (
    <TouchableOpacity
      style={styles.deviceItem}
      onPress={() => newConnectToDevice(item)}
    >
      <Text style={styles.deviceName}>{item.name || 'Unknown Device'}</Text>
      <Text style={styles.deviceId}>{item.id}</Text>
    </TouchableOpacity>
  );

//   useEffect(() => {
//     scanAndConnectDevices();
//   }, []);

  const connectToDevice = (device) => {
    return device
      .connect()
      .then((device) => {
        setDeviceID(device.id);
        setConnectionStatus("Connected");
        deviceRef.current = device;
        return device.discoverAllServicesAndCharacteristics();
      })
      .then((device) => {
        return device.services();
      })
      .then((services) => {
        let service = services.find((service) => service.uuid === SERVICE_UUID);
        return service.characteristics();
      })
      .then((characteristics) => {
        let stepDataCharacteristic = characteristics.find(
          (char) => char.uuid === STEP_DATA_CHAR_UUID
        );
        // setStepDataChar(stepDataCharacteristic);
        stepDataCharacteristic.monitor((error, char) => {
          if (error) {
            console.error(error);
            return;
          }
          const rawStepData = atob(char.value);
          console.log("Received step data:", rawStepData);
          setCount(rawStepData);
        });
      })
      .catch((error) => {
        console.log(error);
        setConnectionStatus("Error in Connection");
      });
  };

  const newConnectToDevice = async (device: Device) => {
    try {
      setConnectionStatus('Connecting...');
      
      // Connect to the device
      await device.connect();
      setDeviceID(device.id);
      setConnectionStatus('Connected');
      
      // Discover services and characteristics
      const connectedDevice = await device.discoverAllServicesAndCharacteristics();
      setConnectionStatus('Discovered services and characteristics');
      
      // Get services
      const services = await connectedDevice.services();
      console.log("what get for services", services);
      // For this example, we'll use the first service
      // In a real app, you might want to look for a specific service or let the user choose
      if (services.length > 0) {
        const service = services[0];
        setServiceUUID(service.uuid);
        console.log('Selected Service UUID:', service.uuid);
        
        // Get characteristics for this service
        const characteristics = await service.characteristics();
        
        // For this example, we'll use the first characteristic that is readable and notifiable
        // Again, in a real app, you might want to be more specific
        const characteristic = characteristics.find(c => c.isReadable && c.isNotifiable);
        // console.log("what get for characteristic", characteristic); 
        if (characteristic) {
          setCharacteristicUUID(characteristic.uuid);
          console.log('Selected Characteristic UUID:', characteristic.uuid);
          setConnectionStatus('Ready for communication');
        } else {
          console.log('No suitable characteristic found');
          setConnectionStatus('No suitable characteristic found');
        }
      } else {
        console.log('No services found');
        setConnectionStatus('No services found');
      }
    } catch (error) {
      console.error('Connection error:', error);
      setConnectionStatus('Connection failed');
    }
  };

  const sendData = async (data: string) => {
    if (!deviceID || !serviceUUID || !characteristicUUID) {
      console.log('Device not fully connected');
      return;
    }

    try {
      const device = await bleManager.devices([deviceID]);
      if (device.length > 0) {
        await device[0].writeCharacteristicWithResponseForService(
          serviceUUID,
          characteristicUUID,
          btoa(data)
        );
        console.log('Data sent successfully');
      }
    } catch (error) {
      console.error('Error sending data:', error);
    }
  };

  const hideModal = () => {
    setIsModalVisible(false);
  };

  // const openModal = async () => {
  //   // scanForDevices();
  //   // setIsModalVisible(true);
    
  // };
//   useEffect(() => {
//     const subscription = bleManager.onDeviceDisconnected(
//       deviceID,
//       (error, device) => {
//         if (error) {
//           console.log("Disconnected with error:", error);
//         }
//         setConnectionStatus("Disconnected");
//         console.log("Disconnected device");
//         setCount(0); // Reset the step count
//         if (deviceRef.current) {
//           setConnectionStatus("Reconnecting...");
//           connectToDevice(deviceRef.current)
//             .then(() => setConnectionStatus("Connected"))
//             .catch((error) => {
//               console.log("Reconnection failed: ", error);
//               setConnectionStatus("Reconnection failed");
//             });
//         }
//       }
//     );
//     return () => subscription.remove();
//   }, [deviceID]);

  const sendAlarmTime = async () => {
    if (alarmTime.trim() === '') {
      console.log('Please enter a valid time');
      return;
    }
    await sendData(alarmTime);
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style = {styles.heartRateTitleWrapper}>
        <Text>count is {Count} </Text>
        <Text>{connectionStatus}</Text>

      </View>
      <TouchableOpacity
        onPress={scanForAvailableDevices}
        style={styles.ctaButton}
        disabled={isScanning}
      >
        <Text style={styles.ctaButtonText}>
          {isScanning ? 'Scanning...' : 'Scan for Devices'}
        </Text>
      </TouchableOpacity>
      <FlatList
        data={allDevices}
        renderItem={renderDeviceItem}
        keyExtractor={(item) => item.id}
        style={styles.deviceList}
      />
      {/* <DeviceModal
        closeModal={hideModal}
        visible={isModalVisible}
        connectToPeripheral={connectToDevice}
        devices={allDevices}
      /> */}
      
      <Text style={styles.label}>Enter Alarm Time (HH:MM):</Text>
      <TextInput
        style={styles.input}
        value={alarmTime}
        onChangeText={setAlarmTime}
        placeholder="e.g., 07:30"
        keyboardType="numbers-and-punctuation"
      />
      
      <TouchableOpacity 
        style={styles.button} 
        onPress={sendAlarmTime}
        disabled={!deviceID || !serviceUUID || !characteristicUUID}
      >
        <Text style={styles.buttonText}>Send Alarm Time to Device</Text>
      </TouchableOpacity>
      
      <Text>Connection Status: {connectionStatus}</Text>
      <Text>Service UUID: {serviceUUID}</Text>
      <Text>Characteristic UUID: {characteristicUUID}</Text>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f2f2f2',
  },
  heartRateTitleWrapper: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  heartRateTitleText: {
    fontSize: 30,
    fontWeight: 'bold',
    textAlign: 'center',
    marginHorizontal: 20,
    color: 'black',
  },
  heartRateText: {
    fontSize: 25,
    marginTop: 15,
  },
  ctaButton: {
    backgroundColor: 'purple',
    justifyContent: 'center',
    alignItems: 'center',
    height: 50,
    marginHorizontal: 20,
    marginBottom: 5,
    borderRadius: 8,
  },
  ctaButtonText: {
    fontSize: 18,
    fontWeight: 'bold',
    color: 'white',
  },
  deviceList: {
    flex: 1,
    marginHorizontal: 20,
  },
  deviceItem: {
    padding: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#ccc',
  },
  deviceName: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  deviceId: {
    fontSize: 12,
    color: '#666',
  },
  button: {
    backgroundColor: '#007AFF',
    padding: 10,
    borderRadius: 5,
    marginVertical: 10,
  },
  buttonText: {
    color: 'white',
    fontSize: 16,
  },
  label: {
    fontSize: 16,
    marginBottom: 5,
  },
  input: {
    width: '100%',
    height: 40,
    borderColor: 'gray',
    borderWidth: 1,
    borderRadius: 5,
    paddingHorizontal: 10,
    marginBottom: 10,
  },
});