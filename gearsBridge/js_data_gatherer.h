
#ifndef JS_DATA_GATHERER_H_
#define JS_DATA_GATHERER_H_

#include "gears/base/npapi/browser_utils.h"
#include "gears/geolocation/device_data_provider.h"
#include "data_gatherer.h"

class JsDataGatherer
    : public ModuleImplBaseClass {
 public:
  JsDataGatherer();
  virtual ~JsDataGatherer();

  void GatherData(JsCallContext *context);

  bool CreateJavaScriptDataObject(
      LocutorSystemInfo &sysInfo,
      JsRunnerInterface *js_runner,
      JsObject *data_object);

};

#endif /* JS_DATA_GATHERER_H_ */
