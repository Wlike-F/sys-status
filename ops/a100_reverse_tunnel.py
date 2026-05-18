import os
import select
import socket
import time

import paramiko


REMOTE_BIND_HOST = "127.0.0.1"
REMOTE_BIND_PORT = 18080
LOCAL_HOST = "127.0.0.1"
LOCAL_PORT = 8080


def forward(channel):
    sock = None
    try:
        sock = socket.create_connection((LOCAL_HOST, LOCAL_PORT), timeout=5)
        while True:
            readable, _, _ = select.select([channel, sock], [], [], 30)
            if channel in readable:
                data = channel.recv(65536)
                if not data:
                    break
                sock.sendall(data)
            if sock in readable:
                data = sock.recv(65536)
                if not data:
                    break
                channel.sendall(data)
    except Exception as exc:
        print(f"forward error: {exc}", flush=True)
    finally:
        for obj in (sock, channel):
            try:
                if obj is not None:
                    obj.close()
            except Exception:
                pass


def run_once():
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(
        hostname=os.environ["A100_HOST"],
        port=int(os.environ.get("A100_PORT", "22")),
        username=os.environ["A100_USER"],
        password=os.environ["A100_PASS"],
        timeout=12,
        banner_timeout=12,
        auth_timeout=12,
    )
    transport = client.get_transport()
    transport.request_port_forward(REMOTE_BIND_HOST, REMOTE_BIND_PORT)
    print(
        f"tunnel ready: {REMOTE_BIND_HOST}:{REMOTE_BIND_PORT} -> {LOCAL_HOST}:{LOCAL_PORT}",
        flush=True,
    )
    while transport.is_active():
        channel = transport.accept(2)
        if channel is None:
            continue
        forward(channel)


while True:
    try:
        run_once()
    except Exception as exc:
        print(f"tunnel error: {exc}", flush=True)
        time.sleep(5)
