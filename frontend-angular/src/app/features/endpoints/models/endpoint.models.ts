import { ActiveFlag } from '../../../core/models/common.models';

export type ParameterType = 'VARCHAR2' | 'NUMBER' | 'DATE' | 'TIMESTAMP';

export interface EndpointParameter {
  name: string;
  type: ParameterType;
  required: boolean;
}

export interface Endpoint {
  id?: number;
  integrationId: number;
  name: string;
  description?: string;
  path: string;
  method: 'GET';
  sqlText: string;
  parameters: EndpointParameter[];
  active: ActiveFlag;
}

export interface EndpointExecutionResult {
  success: boolean;
  status: number;
  statusText: string;
  url: string;
  duration: number;
  data: unknown;
}
