#include "js_data_gatherer.h"
#include "gears/base/common/js_runner.h"
#include "gears/base/common/js_types.h"

DECLARE_DISPATCHER(JsDataGatherer);

template<>
void Dispatcher<JsDataGatherer>::Init() {
	RegisterMethod("gatherData", &JsDataGatherer::GatherData);
}

JsDataGatherer::JsDataGatherer()
  : ModuleImplBaseClass("JsLocutor") {
}

JsDataGatherer::~JsDataGatherer() {
}

void JsDataGatherer::GatherData(JsCallContext *context) {
	int timeout = 1000;

	// get timeout argument as int from function call arguments
	JsArgument argv[] = {
	    { JSPARAM_OPTIONAL, JSPARAM_INT, &timeout },
	};
	context->GetArguments(ARRAYSIZE(argv), argv);
	if (context->is_exception_set()) {
	    LOG(("JsDataGatherer::GatherData(int) : Arguments do not match.\n"));
		return;
	}

	LocutorSystemInfo sysInfo = gatherData(timeout);

    // Create the object for returning to JavaScript.
	scoped_ptr<JsObject> return_object(module_environment_->js_runner_->NewObject());
	// If this method executes during page unload, the call to GetDispID
	// in JsRunnerBase::NewObjectWithArguments() can actually fail, so
	// we end up with a NULL object.
	if (!return_object.get()) {
	    LOG(("JsDataGatherer::GatherData(int) : No return object.\n"));
		return;
	}

	bool ok = CreateJavaScriptDataObject(sysInfo,
			module_environment_->js_runner_, return_object.get());
	if (!ok) {
	    LOG(("JsDataGatherer::GatherData(int) : Failed to create return "
	         "object.\n"));
	    assert(false);
	    return;
	}
	context->SetReturnValue(JSPARAM_OBJECT, return_object.get());
}

// local function
static bool SetObjectPropertyIfValidString(const std::string16 &property_name,
                                           const std::string16 &value,
                                           JsObject *object) {
  assert(object);
  if (!value.empty()) {
    return object->SetPropertyString(property_name, value);
  }
  return true;
}

bool JsDataGatherer::CreateJavaScriptDataObject(
	LocutorSystemInfo &sysInfo,
    JsRunnerInterface *js_runner,
    JsObject *data_object) {

	scoped_ptr<JsArray> wifi_object(js_runner->NewArray());
	if (!wifi_object.get()) {
	  assert(false);
	  return false;
	}

    bool result = true;

	result &= data_object->SetPropertyArray(STRING16(L"accessPoints"),
                                               wifi_object.get());

    int count = 0;
	for (AccessPointDataSet::const_iterator iter = sysInfo.wifiData.access_point_data.begin();
         iter != sysInfo.wifiData.access_point_data.end();
         iter++) {
		AccessPointData data = *iter;

        scoped_ptr<JsObject> data_object(js_runner->NewObject());
		if (!data_object.get()) {
		    assert(false);
		    return false;
		}

		result &= SetObjectPropertyIfValidString(STRING16(L"macAddress"),
		                                           data.mac_address,
		                                           data_object.get());
		// values may exceed int boundaries of JavaScript and lead to
		// undetermined behavior
		result &= data_object->SetPropertyDouble(STRING16(L"radioSignalStrength"),
		                                               data.radio_signal_strength);
		result &= data_object->SetPropertyDouble(STRING16(L"age"),
		                                               data.age);
//		result &= data_object->SetPropertyBool(STRING16(L"isAgeDefined"),
//				!isUndefinedWifiValue(data.age));
		result &= data_object->SetPropertyDouble(STRING16(L"channel"),
		                                               data.channel);
//		result &= data_object->SetPropertyBool(STRING16(L"isChannelDefined"),
//				!isUndefinedWifiValue(data.channel));
		result &= data_object->SetPropertyDouble(STRING16(L"signalToNoise"),
		                                               data.signal_to_noise);
//		result &= data_object->SetPropertyBool(STRING16(L"isSignalToNoiseDefined"),
//				!isUndefinedWifiValue(data.signal_to_noise));
		result &= SetObjectPropertyIfValidString(STRING16(L"ssid"),
		                                           data.ssid,
		                                           data_object.get());

		result &= wifi_object->SetElementObject(count, data_object.get());
        count++;
	}

    scoped_ptr<JsObject> hardware_object(js_runner->NewObject());
	if (!hardware_object.get()) {
	    assert(false);
	    return false;
	}

	result &= data_object->SetPropertyObject(STRING16(L"hardware"),
                                               hardware_object.get());

	result &= hardware_object->SetPropertyDouble(STRING16(L"screenWidth"),
	                                               sysInfo.screenWidth);

	result &= hardware_object->SetPropertyDouble(STRING16(L"screenHeight"),
	                                               sysInfo.screenHeight);

	result &= hardware_object->SetPropertyDouble(STRING16(L"screenResolution"),
	                                               sysInfo.screenResolution);

	result &= hardware_object->SetPropertyDouble(STRING16(L"memorySize"),
	                                               sysInfo.memorySize);

	result &= hardware_object->SetPropertyDouble(STRING16(L"memoryUse"),
	                                               sysInfo.memoryUse);

	result &= hardware_object->SetPropertyDouble(STRING16(L"processorSpeed"),
	                                               sysInfo.processorSpeed);

	result &= hardware_object->SetPropertyDouble(STRING16(L"maxProcessorSpeed"),
	                                               sysInfo.maxProcessorSpeed);

	result &= hardware_object->SetPropertyDouble(STRING16(L"netSpeed"),
	                                               sysInfo.netSpeed);

	result &= hardware_object->SetPropertyDouble(STRING16(L"maxNetSpeed"),
	                                               sysInfo.maxNetSpeed);

	result &= hardware_object->SetPropertyDouble(STRING16(L"systemLoad"),
	                                               sysInfo.systemLoad);

	result &= hardware_object->SetPropertyDouble(STRING16(L"volumeUse"),
	                                               sysInfo.volumeUse);

	result &= hardware_object->SetPropertyDouble(STRING16(L"volumeUse"),
	                                               sysInfo.volumeUse);

	if (sysInfo.hasSystemBattery) {
		scoped_ptr<JsObject> battery_object(js_runner->NewObject());
		if (!battery_object.get()) {
			assert(false);
			return false;
		}

		result &= hardware_object->SetPropertyObject(STRING16(L"battery"),
												   battery_object.get());

		result &= hardware_object->SetPropertyDouble(STRING16(L"lifePercent"),
		                                               sysInfo.batteryLifePercent);

		result &= hardware_object->SetPropertyDouble(STRING16(L"lifeTime"),
		                                               sysInfo.batteryLifeTime);

		result &= hardware_object->SetPropertyDouble(STRING16(L"powerPlug"),
		                                               sysInfo.powerPlugStatus);
	}

	return result;
}

