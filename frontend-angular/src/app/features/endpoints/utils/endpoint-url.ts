export function buildEndpointUrl(
  basePath: string,
  endpointPath: string,
  values: Readonly<Record<string, string | null | undefined>>,
): string {
  const normalizedBasePath = basePath.replace(/\/$/, '');
  const normalizedEndpointPath = endpointPath.replace(/^\//, '');
  const query = new URLSearchParams();

  Object.entries(values).forEach(([name, value]) => {
    if (value !== '' && value !== null && value !== undefined) query.append(name, value);
  });

  return `${normalizedBasePath}/${normalizedEndpointPath}${query.size ? `?${query.toString()}` : ''}`;
}
