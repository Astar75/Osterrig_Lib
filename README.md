Osterrig Lib
---
Библиотека для управления лампами Osterrig. 
В текущий момент находится в состоянии разработки. 

Управление лампами осуществляется по Bluetooth LE.

В текущий момент поддерживает лампы Sirius, Solaris, Stalker. В будущем планируется добавить поддержку WALS и TC типов ламп.

## Фичи реализованные в текущий момент
1. Установка яркости лампы
2. Установка цвета в формате RGB и RGBCW
   
## Использование

```
override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  // ...
  val btAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
  val btDevice: BluetoothDevice = btAdapter.getRemoteDevice(DEVICE_MAC_ADDRESS)
  val sirius: Sirius = Sirius(context = context, device = btDevice)
  lifecycleScope.launch { 
    sirius.connect()
    sirius.setBrightness(64)
    sirius.setColor(RgbColor(255, 0, 255)) 
  }
}
```
