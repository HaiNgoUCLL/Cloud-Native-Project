import Cookies from 'js-cookie';
import api from '@/lib/axios';

jest.mock('js-cookie', () => ({
  get: jest.fn(),
  remove: jest.fn(),
  set: jest.fn(),
}));

describe('axios interceptors', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('adds auth header when token exists', async () => {
    (Cookies.get as jest.Mock).mockReturnValue('test-token');

    // The request interceptor modifies config
    const interceptors = (api.interceptors.request as any).handlers;
    const requestInterceptor = interceptors[0];
    const config = { headers: {} as Record<string, string> };
    const result = requestInterceptor.fulfilled(config);

    expect(result.headers.Authorization).toBe('Bearer test-token');
  });

  it('does not add auth header when no token', async () => {
    (Cookies.get as jest.Mock).mockReturnValue(undefined);

    const interceptors = (api.interceptors.request as any).handlers;
    const requestInterceptor = interceptors[0];
    const config = { headers: {} as Record<string, string> };
    const result = requestInterceptor.fulfilled(config);

    expect(result.headers.Authorization).toBeUndefined();
  });

  it('clears token on 401 response', async () => {
    const interceptors = (api.interceptors.response as any).handlers;
    const responseInterceptor = interceptors[0];
    const error = { response: { status: 401 } };

    await expect(responseInterceptor.rejected(error)).rejects.toEqual(error);
    expect(Cookies.remove).toHaveBeenCalledWith('token');
    expect(Cookies.remove).toHaveBeenCalledWith('user');
  });
});
