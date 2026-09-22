INSERT INTO server (user_id, name, server_type, server_url)
VALUES
    (NULL, '8x8 Jitsi Meet', 'DEFAULT', 'https://meet.jit.si'),
    (NULL, 'Brave Talk', 'DEFAULT', 'https://talk.brave.com'),
    (NULL, 'IONOS Video Chat', 'DEFAULT', 'https://meet.videochat.ionos.com'),
    (NULL, 'openSUSE Meet', 'DEFAULT', 'https://meet.opensuse.org'),
    (NULL, 'Free Software Foundation (FSF)', 'DEFAULT', 'https://jitsi.member.fsf.org'),
    (NULL, 'Freifunk München (FFMUC)', 'DEFAULT', 'https://meet.ffmuc.net'),
    (NULL, 'IN-Berlin Meet', 'DEFAULT', 'https://meet.in-berlin.de'),
    (NULL, 'Systemli Meet', 'DEFAULT', 'https://meet.systemli.org'),
    (NULL, 'vpsFree.cz Meet', 'DEFAULT', 'https://meet.vpsfree.cz'),
    (NULL, 'OpenCloud LU', 'DEFAULT', 'https://jitsi.opencloud.lu'),
    (NULL, 'FSLab Meet', 'DEFAULT', 'https://meet.fslab.de'),
    (NULL, '4D2 Jitsi', 'DEFAULT', 'https://jitsi.4d2.org')
ON CONFLICT (user_id, server_url) DO NOTHING