import {
  de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest,
  de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest$UserMedium,
  RestApplicationClient,
} from "./emil.js";

const client = new RestApplicationClient(null);

const a = await client.getEnvironment("abc");

client.createComponent({
  drives: [
    {
      bootable: true,
      data: {
        kind: "x",
        mediumType: "CDROM",
      } as de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest$UserMedium,
    },
  ],
  environment: "",
  inputMedia: "",
} as de_bwl_bwfla_emil_datatypes_rest_MachineComponentRequest);
